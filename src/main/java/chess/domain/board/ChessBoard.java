package chess.domain.board;

import chess.domain.piece.Piece;
import chess.domain.piece.position.PiecePosition;
import chess.domain.piece.position.WayPoints;

import chess.domain.state.ChessState;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessBoard {

    private final List<Piece> pieces;

    public ChessBoard(final List<Piece> pieces) {
        this.pieces = new ArrayList<>(pieces);
    }

    public void movePiece(final ChessState state, final PiecePosition source, final PiecePosition destination) {
        final Piece from = get(source);
        validateCorrectTurn(state, from);
        validateNonBlock(destination, from);
        moveOrKill(destination, from);
    }

    private void validateCorrectTurn(final ChessState state, final Piece from) {
        if (state.isInCorrectTurn(from.color())) {
            throw new IllegalArgumentException("상대 말 선택하셨습니다.");
        }
    }

    private void validateNonBlock(final PiecePosition destination, final Piece from) {
        final WayPoints wayPoints = from.wayPointsWithCondition(destination);
        if (wayPoints.isBlocking(pieces)) {
            throw new IllegalArgumentException("경로 상에 말이 있어서 이동할 수 없습니다.");
        }
    }

    private void moveOrKill(final PiecePosition destination, final Piece from) {
        if (existByPosition(destination)) {
            final Piece to = get(destination);
            from.moveAndKill(to);
            pieces.remove(to);
            return;
        }
        from.move(destination);
    }

    private boolean existByPosition(final PiecePosition piecePosition) {
        return pieces.stream()
                .anyMatch(piece -> piece.existIn(piecePosition));
    }

    public Piece get(final PiecePosition piecePosition) {
        return pieces.stream()
                .filter(piece -> piece.existIn(piecePosition))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("해당 위치에 존재하는 피스가 없습니다."));
    }

    public List<Piece> pieces() {
        return pieces.stream()
                .map(Piece::clone)
                .collect(Collectors.toList());
    }
}
