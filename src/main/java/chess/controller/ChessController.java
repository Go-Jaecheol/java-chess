package chess.controller;

import chess.domain.board.ChessBoard;
import chess.domain.board.ChessBoardFactory;
import chess.domain.piece.Color;
import chess.domain.piece.position.PiecePosition;
import chess.domain.state.ChessState;
import chess.domain.state.Initialize;
import chess.domain.state.Run;
import chess.domain.state.command.Command;
import chess.service.PieceService;
import chess.service.GameService;
import chess.view.InputView;
import chess.view.OutputView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChessController {

    private final GameService gameService;
    private final PieceService pieceService;

    public ChessController(final GameService gameService, final PieceService pieceService) {
        this.gameService = gameService;
        this.pieceService = pieceService;
    }

    public void start() {
        OutputView.printStartMessage();
        final List<Integer> gameIds = gameService.findAllIds();
        initializeWithDB(gameIds);
    }

    private void initializeWithDB(List<Integer> gameIds) {
        if (gameIds.isEmpty()) {
            final ChessBoard chessBoard = ChessBoardFactory.create();
            gameService.create();
            pieceService.create(chessBoard, 1);
            final ChessState state = new Initialize();
            run(chessBoard, state, 1);
            return;
        }
        final ChessBoard chessBoard = new ChessBoard(pieceService.findAllPieces());
        final ChessState state = new Run(gameService.findTurnById(gameIds.get(0)));
        run(chessBoard, state, gameIds.get(0));
    }

    private void run(final ChessBoard chessBoard, ChessState state, final int gameId) {
        while (state.isRunnable()) {
            state = chessState(chessBoard, state);
        }
        pieceService.deleteAll();
        gameService.delete(gameId);
        OutputView.printResult(state.findCurrentTurn());
    }

    private ChessState chessState(final ChessBoard chessBoard, ChessState state) {
        try {
            final Command command = Command.parse(new ArrayList<>(InputView.readCommand()));
            state = executeByCommand(chessBoard, command, state);
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
        return state;
    }

    private ChessState executeByCommand(final ChessBoard chessBoard, final Command command, ChessState state) {
        if (command.isSave()) {
            gameService.update(state.findCurrentTurn());
            pieceService.deleteAll();
            pieceService.create(chessBoard, 1);
        }
        if (command.isStatus()) {
            calculateScore(chessBoard);
            return state;
        }
        state = changeState(chessBoard, command, state);
        gameService.update(state.findCurrentTurn());
        OutputView.showBoard(chessBoard.pieces());
        return state;
    }

    private void calculateScore(final ChessBoard chessBoard) {
        final Map<Color, Double> scoreByColor = chessBoard.calculateScore();
        OutputView.printTeamScore(Color.WHITE, scoreByColor.get(Color.WHITE));
        OutputView.printTeamScore(Color.BLACK, scoreByColor.get(Color.BLACK));
    }

    private ChessState changeState(final ChessBoard chessBoard, final Command command, ChessState state) {
        state = state.changeStateByCommand(command);
        if (command.isMove()) {
            state = movePiece(chessBoard, command, state);
        }
        return state;
    }

    private ChessState movePiece(final ChessBoard chessBoard, final Command command, ChessState state) {
        final PiecePosition from = PiecePosition.of(command.getFromParameter());
        final PiecePosition to = PiecePosition.of(command.getToParameter());
        state = chessBoard.movePiece(state, from, to);
        if (state.isRunnable()) {
            return state.changeTurn();
        }
        return state;
    }
}
