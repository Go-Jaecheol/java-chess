package chess.domain.piece.role;

import chess.domain.piece.Color;
import chess.domain.piece.Piece;
import chess.domain.piece.Role;
import chess.domain.piece.position.WayPoints;
import chess.domain.piece.position.Path;
import chess.domain.piece.position.PiecePosition;

public class Rook extends Piece {

    public Rook(final Color color, final PiecePosition piecePosition) {
        super(color, piecePosition);
        this.role = Role.ROOK;
    }

    @Override
    protected void validateMovable(final Path path) {
        if (!path.isStraight()) {
            throw new IllegalArgumentException("록은 직선으로만 이동할 수 있습니다.");
        }
    }

    @Override
    protected WayPoints wayPointsWithCondition(final Path path) {
        return new WayPoints(path.wayPoints());
    }

    @Override
    public boolean isKing() {
        return false;
    }

    @Override
    public boolean isPawn() {
        return false;
    }

    @Override
    public double score() {
        return 5;
    }
}
