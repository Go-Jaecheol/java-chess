
## 클래스 다이어그램

### 주요 클래스 구조

```mermaid
classDiagram
  class ChessBoard {
    -List~Piece~ pieces
  }
  
  class Piece
  <<Abstract>> Piece
  
  class Color
  <<Enumeration>> Color
  
  class Direction
  <<Enumeration>> Direction
  
  ChessController ..> ChessBoard
  ChessController ..> ChessState
  
  ChessBoard --> Piece
  ChessBoard ..> PiecePosition
  ChessBoard ..> WayPoints
  ChessBoard ..> Turn
  
  Piece --> Color
  Piece --> PiecePosition
  Piece ..> Path
는
  
  Path --> PiecePosition
  WayPoints --> PiecePosition
  
  PiecePosition --> Rank
  PiecePosition --> File
  PiecePosition ..> Direction
```

### Piece 추상 클래스
```mermaid
classDiagram
  class Piece
  <<Abstract>> Piece
  
  Piece <|-- King
  Piece <|-- Queen
  Piece <|-- Bishop
  Piece <|-- Rook
  Piece <|-- Knight
  Piece <|-- Pawn
```

### 상태 패턴
```mermaid
classDiagram
  class ChessState
  <<Interface>> ChessState
  
  class AbstractChessState
  <<Abstract>> AbstractChessState
  
  ChessState ..> Command
  ChessState <|.. AbstractChessState
  AbstractChessState <|-- Initialize
  AbstractChessState <|-- Running
  AbstractChessState <|-- End
  Command --> Type
```

---

## 1단계 기능 요구 사항

- [x] 게임 시작 메세지 출력
- [x] 커맨드 입력 메세지 출력
- [x] 커맨드 입력 기능
- [x] 체스판 출력
- [x] 체스판 만드는 기능

### 기물

- [x] 위치를 갖는다.
- [x] 종류는 킹, 퀸, 룩, 나이트, 비숍, 폰 이 있다.

### 체스 판

- [x] 가로는 Rank 이다.
- [x] 세로는 File 이다.
- [x] 8 X 8 이다.

---

## 2단계 기능 요구 사항

- [x] 게임 시작 및 커맨드 메세지 출력
- [x] 이동 커맨드 입력 기능 추가
- [x] White, Black 턴 반복 기능 추가
- [x] start, move, end 커맨드에 대한 로직 구현

### 기물 이동

- 공통
  - [x] 도착지에 아군 기물이 있을 경우, 이동 불가능
  - [x] 도착지에 상대 기물이 있을 경우, 이동 후 상대 기물 제거(Pawn 예외)
  - [x] 도착지까지 가는 경로에 기물이 있을 경우, 이동 불가능(Knight 예외)
  - [x] 상대 말 이동 불가능
- King
  - [x] 주변 한 칸만 이동 가능
- Queen
  - [x] 직선 거리 or 대각선 거리로 이동 가능
- Bishop
  - [x] 대각선 거리로 이동 가능
- Rook
  - [x] 직선 거리로 이동 가능
- Knight
  - [x] 앞으로 두 칸 -> 옆으로 한 칸 이동 가능 (8개)
- Pawn
  - [x] 첫 이동인 경우, 앞으로 두 칸까지 이동 가능
  - [x] 첫 이동이 아닌 경우, 앞으로 한 칸만 이동 가능
  - [x] 앞에 상대 기물이 있는 경우, 이동 불가능
  - [x] 대각선 위치에 상대 기물이 있는 경우, 이동 후 상대 기물 제거 가능
