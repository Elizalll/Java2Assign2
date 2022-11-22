package application.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static int I_AM;
    private static final int PLAY_1 = 1;
    private static final int PLAY_2 = 2;
    private static final int EMPTY = 0;
    private static final int BOUND = 90;
    private static final int OFFSET = 15;
    public Label label;
    @FXML
    private Pane base_square;
    @FXML
    private Rectangle game_panel;
    private static boolean TURN = true, GAME_START = false, TURN_GAME = false;
    private static final int[][] chessBoard = new int[3][3];
    private static final boolean[][] flag = new boolean[3][3];

    Socket socket;
    PrintWriter pw;
    BufferedReader br;
    String name = "A";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 1234);
            pw = new PrintWriter(socket.getOutputStream());
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw.write("Client " + name + " here, I wants to play.\nEND\n");
            pw.flush();
            System.out.println(ReadOneSentence(br));// First sentence from the server
            String info;
            do {
                info = ReadOneSentence(br);
                if (info.length() > 0) {
                    System.out.println(name + " " + info);
                }
            } while (!info.equals("Matched."));

            info = ReadOneSentence(br);
            if (Objects.equals(info, "Player 1")) {
                I_AM = PLAY_1;
                System.out.println("I am player 1");
                showTurnInfo();
                GAME_START = true;
            } else if (Objects.equals(info, "Player 2")) {
                I_AM = PLAY_2;
                TURN = false;
                System.out.println("I am player 2");
                showTurnInfo();
                if (!TURN && !GAME_START) {
                    GAME_START = true;
                    System.out.println("GAME_START = true;");
                    String[] infoij;
                    try {
                        infoij = ReadOneSentence(br).split(",");
                        System.out.println("info0" + Arrays.toString(infoij));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    int i = Integer.parseInt(infoij[0]), j = Integer.parseInt(infoij[1]);
                    if (refreshBoard(i, j)) {
                        TURN = true;
                        showTurnInfo();
                    }
                }
            } else {
                System.out.println("Wrong in I_AM");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        game_panel.setOnMouseClicked(event -> {
            if (TURN && GAME_START) {
                int x = (int) (event.getX() / BOUND);
                int y = (int) (event.getY() / BOUND);
                if (refreshBoard(x, y)) {
                    TURN = false;
                    showTurnInfo();
                    pw.write(x + "," + y + "\nEND\n");
                    pw.flush();
                    if (!TURN && GAME_START) {
                        try {
                            String[] infoij;
                            String info;
                            info = ReadOneSentence(br);
                            System.out.println("info1:" + info);

                            switch (info) {
                                case "Continue" -> {
                                    info = ReadOneSentence(br);
                                    System.out.println("info2:" + info);
                                    infoij = info.split(",");
                                    int i = Integer.parseInt(infoij[0]), j = Integer.parseInt(infoij[1]);
                                    if (refreshBoard(i, j)) {

                                        info = ReadOneSentence(br);
                                        System.out.println("info3:" + info);
                                        switch (info) {
                                            case "Continue" -> {
                                                TURN = true;
                                                showTurnInfo();
                                            }
                                            case "Win" -> showWinInfo(true);
                                            case "Loss" -> showWinInfo(false);
                                            default -> System.out.println("Wrong in inner Win switch");
                                        }
                                    }
                                }
                                case "Win" -> showWinInfo(true);
                                case "Loss" -> showWinInfo(false);
                                default -> System.out.println("Wrong in Win switch");
                            }

                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }


    String ReadOneSentence(BufferedReader br) throws IOException, InterruptedException {
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null && !"END".equals(line)) {
            sb.append(line);
            Thread.sleep(100);
        }
        return sb.toString();
    }

    void showTurnInfo() {
        label.setText("Turn: " + (TURN ? "your" : "opposite"));
    }

    void showWinInfo(boolean win) {
        label.setText("Winner: " + (win ? "you" : "opposite"));
    }

    private boolean refreshBoard(int x, int y) {
        if (chessBoard[x][y] == EMPTY) {
            chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
            drawChess();
            return true;
        }
        return false;
    }

    private void drawChess() {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (flag[i][j]) {
                    // This square has been drawing, ignore.
                    continue;
                }
                switch (chessBoard[i][j]) {
                    case PLAY_1:
                        drawCircle(i, j);
                        break;
                    case PLAY_2:
                        drawLine(i, j);
                        break;
                    case EMPTY:
                        // do nothing
                        break;
                    default:
                        System.err.println("Invalid value!");
                }
            }
        }
    }

    private void drawCircle(int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
        flag[i][j] = true;
    }

    private void drawLine(int i, int j) {
        Line line_a = new Line();
        Line line_b = new Line();
        base_square.getChildren().add(line_a);
        base_square.getChildren().add(line_b);
        line_a.setStartX(i * BOUND + OFFSET * 1.5);
        line_a.setStartY(j * BOUND + OFFSET * 1.5);
        line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_a.setStroke(Color.BLUE);

        line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        line_b.setStartY(j * BOUND + OFFSET * 1.5);
        line_b.setEndX(i * BOUND + OFFSET * 1.5);
        line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_b.setStroke(Color.BLUE);
        flag[i][j] = true;
    }
}
