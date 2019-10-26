/*
 * Copyright (c) 2016—2017 Andrei Tomashpolskiy and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bt.protocol;

import com.google.common.base.MoreObjects;

/**
 * @since 1.0
 */
public final class Piece implements Message {

    private int pieceIndex;
    private int offset;
    private int length;
    private byte[] block;

    /**
     * @since 1.0
     */
    public Piece(int pieceIndex, int offset, byte[] block) throws InvalidMessageException {

        if (pieceIndex < 0 || offset < 0 || block.length == 0) {
            throw new InvalidMessageException("Invalid arguments: piece index (" +
                    pieceIndex + "), offset (" + offset + "), block length (" + block.length + ")");
        }
        this.pieceIndex = pieceIndex;
        this.offset = offset;
        this.length = block.length;
        this.block = block;
    }

    // TODO: Temporary (used only for incoming pieces)
    public Piece(int pieceIndex, int offset, int length) throws InvalidMessageException {

        if (pieceIndex < 0 || offset < 0 || length <= 0) {
            throw new InvalidMessageException("Invalid arguments: piece index (" +
                    pieceIndex + "), offset (" + offset + "), block length (" + length + ")");
        }
        this.pieceIndex = pieceIndex;
        this.offset = offset;
        this.length = length;
    }

    /**
     * @since 1.0
     */
    public int getPieceIndex() {
        return pieceIndex;
    }

    /**
     * @since 1.0
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @since 1.9
     */
    public int getLength() {
        return length;
    }

    /**
     * @since 1.0
     */
    public byte[] getBlock() {
        return block;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pieceIndex", pieceIndex)
                .add("offset", offset)
                .add("length", length)
                .toString();
    }

    @Override
    public Integer getMessageId() {
        return StandardBittorrentProtocol.PIECE_ID;
    }
}
