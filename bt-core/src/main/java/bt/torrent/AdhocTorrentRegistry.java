package bt.torrent;

import bt.data.Storage;
import bt.data.IDataDescriptorFactory;
import bt.metainfo.Torrent;
import bt.metainfo.TorrentId;
import bt.service.IRuntimeLifecycleBinder;
import bt.tracker.ITrackerService;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple in-memory torrent registry, that creates new descriptors upon request.
 *
 *<p><b>Note that this class implements a service.
 * Hence, is not a part of the public API and is a subject to change.</b></p>
 */
public class AdhocTorrentRegistry implements TorrentRegistry {

    private ITrackerService trackerService;
    private IDataDescriptorFactory dataDescriptorFactory;
    private IRuntimeLifecycleBinder lifecycleBinder;

    private Set<TorrentId> torrentIds;
    private ConcurrentMap<TorrentId, Torrent> torrents;
    private ConcurrentMap<TorrentId, TorrentDescriptor> descriptors;

    @Inject
    public AdhocTorrentRegistry(ITrackerService trackerService,
                                IDataDescriptorFactory dataDescriptorFactory,
                                IRuntimeLifecycleBinder lifecycleBinder) {

        this.trackerService = trackerService;
        this.dataDescriptorFactory = dataDescriptorFactory;
        this.lifecycleBinder = lifecycleBinder;

        this.torrentIds = ConcurrentHashMap.newKeySet();
        this.torrents = new ConcurrentHashMap<>();
        this.descriptors = new ConcurrentHashMap<>();
    }

    @Override
    public Collection<Torrent> getTorrents() {
        return torrents.values();
    }

    @Override
    public Collection<TorrentId> getTorrentIds() {
        return torrentIds;
    }

    @Override
    public Optional<Torrent> getTorrent(TorrentId torrentId) {
        return Optional.ofNullable(torrents.get(torrentId));
    }

    @Override
    public Optional<TorrentDescriptor> getDescriptor(Torrent torrent) {
        return Optional.ofNullable(descriptors.get(torrent.getTorrentId()));
    }

    @Override
    public Optional<TorrentDescriptor> getDescriptor(TorrentId torrentId) {
        return Optional.ofNullable(descriptors.get(torrentId));
    }

    @Override
    public TorrentDescriptor getOrCreateDescriptor(Torrent torrent, Storage storage) {
        return register(torrent, storage);
    }

    @Override
    public TorrentDescriptor register(Torrent torrent, Storage storage) {
        TorrentId torrentId = torrent.getTorrentId();
        return getDescriptor(torrentId).orElseGet(() -> {
            TorrentDescriptor descriptor = new DefaultTorrentDescriptor(trackerService, torrent,
                    dataDescriptorFactory.createDescriptor(torrent, storage));
            TorrentDescriptor existing = descriptors.putIfAbsent(torrentId, descriptor);
            if (existing != null) {
                descriptor = existing;
            } else {
                final TorrentDescriptor tDescriptor = descriptor;
                lifecycleBinder.onShutdown("Closing torrent data descriptor: " + tDescriptor.getDataDescriptor().toString(), () -> {
                    try {
                        tDescriptor.getDataDescriptor().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            torrentIds.add(torrentId);
            torrents.putIfAbsent(torrentId, torrent);

            return descriptor;
        });
    }
}