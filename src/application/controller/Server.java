package application.controller;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//public class Server {
//
//    public static List<Integer> list =new ArrayList<>();
//
//    public static void main(String[] args) {
//        try {
//            ServerSocket serverSocket = new ServerSocket(1234);
//            Socket socket;
//
//            while (true) {
//                socket = serverSocket.accept();
//                ServerThread thread = new ServerThread(socket);
//                thread.start();
//
//            }
//        } catch (Exception e) {
//            // TODO: handle exception
//            e.printStackTrace();
//        }
//    }
//}

class Server extends Thread {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            Socket socket;
            Server runnable = new Server();
            int i = 0;
            while (true) {
                socket = serverSocket.accept();
                skList.add(socket);
                brList.add(new BufferedReader(new InputStreamReader(socket.getInputStream())));
                pwList.add(new PrintWriter(socket.getOutputStream()));

                new Thread(runnable, String.valueOf(i)).start();
                i += 1;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    static List<Socket> skList = new ArrayList<>();
    static List<BufferedReader> brList = new ArrayList<>();
    static List<PrintWriter> pwList = new ArrayList<>();
    List<Integer> waitingQ = new ArrayList<>();
    //    int waitingNum = 0;
    Lock l = new ReentrantLock();
    Condition c = l.newCondition();

    String ReadOneSentence(BufferedReader br) throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null && !"END".equals(line)) {
            sb.append(line);
        }
        return sb.toString();
    }

    boolean testWin(boolean turn, int[][] chessBoard) {
        int now = turn ? 1 : 2;
        return chessBoard[0][0] == now && chessBoard[0][1] == now && chessBoard[0][2] == now
                || chessBoard[1][0] == now && chessBoard[1][1] == now && chessBoard[1][2] == now
                || chessBoard[2][0] == now && chessBoard[2][1] == now && chessBoard[2][2] == now
                || chessBoard[0][0] == now && chessBoard[1][0] == now && chessBoard[2][0] == now
                || chessBoard[0][1] == now && chessBoard[1][1] == now && chessBoard[2][1] == now
                || chessBoard[0][2] == now && chessBoard[1][2] == now && chessBoard[2][2] == now
                || chessBoard[0][0] == now && chessBoard[1][1] == now && chessBoard[2][2] == now
                || chessBoard[0][2] == now && chessBoard[1][1] == now && chessBoard[2][0] == now;
    }


    @Override
    public void run() {
        int index = Integer.parseInt(Thread.currentThread().getName());
        int oppIndex = -1;
        Socket socket = skList.get(index), socket_opp = null;
        BufferedReader br = brList.get(index), br_opp = null;
        PrintWriter pw = pwList.get(index), pw_opp = null;
        try {
            System.out.println(ReadOneSentence(br));// First sentence from a client
            pw.write("Welcome to the game.\nEND\n");
            pw.flush();


            l.lock();
            System.out.println(index + "-1- " + Arrays.toString(waitingQ.toArray()));
            if (waitingQ.size() > 0) {
                oppIndex = waitingQ.remove(0);
                c.signal();
                pw.write("Matched.\nEND\n");
                pw.flush();
            } else {
                pw.write("Please wait for a match.\nEND\n");
                pw.flush();
                waitingQ.add(index);
                System.out.println(index + "-2- " + Arrays.toString(waitingQ.toArray()));
                c.await();
                if (waitingQ.contains(index)) {
                    System.out.println("Wrong in match");
                }
                System.out.println(index + "-3- " + Arrays.toString(waitingQ.toArray()));
                pw.write("Matched.\nEND\n");
                pw.flush();
            }
            l.unlock();
            System.out.println(index + " " + oppIndex);

            if (oppIndex != -1) {
                socket_opp = skList.get(oppIndex);
                br_opp = brList.get(oppIndex);
                pw_opp = pwList.get(oppIndex);
                sleep(1000);
                pw.write("Player 1\nEND\n");
                pw.flush();
                pw_opp.write("Player 2\nEND\n");
                pw_opp.flush();

                boolean turn = false;
                String info;
                String[] info2;
                int[][] chessBoard = new int[3][3];
                int i, j, chessNum = 0;
                boolean winTest = false;
                do {
                    turn = !turn;
                    if (turn) {
                        info = ReadOneSentence(br);
                        if (info.length() == 0) {
                            pw_opp.write("Oppo Exit");
                            pw_opp.flush();
                            break;
                        }
                    } else {
                        info = ReadOneSentence(br_opp);
                        if (info.length() == 0) {
                            pw.write("Oppo Exit");
                            pw.flush();
                            break;
                        }
                    }
                    System.out.println("receive info: " + turn + " " + info);

                    info2 = info.split(",");
                    i = Integer.parseInt(info2[0]);
                    j = Integer.parseInt(info2[1]);
                    chessBoard[i][j] = turn ? 1 : 2;
                    chessNum += 1;

                    winTest = testWin(turn, chessBoard);
                    if (!turn) {
                        pw.write(i + "," + j + "\nEND\n");
                        pw.flush();
                        System.out.println("Sent ij to " + turn + " " + index);
                        if (winTest) {
                            pw.write("Loss\nEND\n");
                            pw.flush();
                            pw_opp.write("Win\nEND\n");
                            pw_opp.flush();
                        } else {
                            pw.write("Continue\nEND\n");
                            pw.flush();
                            pw_opp.write("Continue\nEND\n");
                            pw_opp.flush();
                            System.out.println("Sent conti to " + index);
                            System.out.println("Sent conti to " + oppIndex);
                        }
                    } else {
                        pw_opp.write(i + "," + j + "\nEND\n");
                        pw_opp.flush();
                        System.out.println("Sent ij to " + turn + " " + oppIndex);
                        if (winTest) {
                            pw.write("Win\nEND\n");
                            pw.flush();
                            pw_opp.write("Loss\nEND\n");
                            pw_opp.flush();
                        } else {
                            if (chessNum == 9) {
                                pw.write("Draw\nEND\n");
                                pw.flush();
                                pw_opp.write("Draw\nEND\n");
                                pw_opp.flush();
                            } else {
                                pw.write("Continue\nEND\n");
                                pw.flush();
                                pw_opp.write("Continue\nEND\n");
                                pw_opp.flush();
                                System.out.println("Sent conti to " + index);
                                System.out.println("Sent conti to " + oppIndex);
                            }
                        }
                    }
                } while (!winTest);


                //Forwarding

            }
        } catch (SocketException e) {
            System.out.println("Socket closed");
        } catch (IOException e) {
            System.out.println("Stream closed");
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted");
        } finally {
            try {
                if (oppIndex != -1) {
                    if (pw != null)
                        pw.close();
                    if (br != null)
                        br.close();
                    if (socket != null)
                        socket.close();
                    if (pw_opp != null)
                        pw_opp.close();
                    if (br_opp != null)
                        br_opp.close();
                    if (socket_opp != null)
                        socket_opp.close();
                }
            } catch (SocketException e) {
                System.out.println("Socket closed");
            } catch (IOException e) {
                System.out.println("Stream closed");
                e.printStackTrace();
            }
        }
    }

}
