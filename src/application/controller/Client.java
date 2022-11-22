//package application.controller;
//
//import application.Main;
//import javafx.fxml.Initializable;
//
//import java.io.*;
//import java.net.Socket;
//import java.net.URL;
//import java.util.Objects;
//import java.util.ResourceBundle;
//
//public class Client {
//    public static void main(String[] args) {
//        new Thread(new Client("A")).start();
////        new Thread(new Client("B")).start();
//
////        new Thread(new Client("C")).start();
////        new Thread(new Client("D")).start();
//
//    }
//
//    private final String name;
//
//    public Client(String name) {
//        this.name = name;
//    }
//
//    String ReadOneSentence(BufferedReader br) throws IOException {
//        String line;
//        StringBuilder sb = new StringBuilder();
//        while ((line = br.readLine()) != null && !"END".equals(line)) {
//            sb.append(line);
//        }
//        return sb.toString();
//    }
//
//    @Override
//    public void run() {
//
//        System.out.println("-------" + name + "-------");
//        while (true)
//            try {
//                if (Objects.equals(name, "B")) {
//                    Thread.sleep(1000);
//                }
////            Socket socket = new Socket("localhost", 1234);
////
////            PrintWriter pw = new PrintWriter(socket.getOutputStream());
////            pw.write("Client " + name + " here, I wants to play.\nEND\n");
////            pw.flush();
////
////            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
////            System.out.println(ReadOneSentence(br));// First sentence from the server
////            String info;
////            do {
////                info = ReadOneSentence(br);
////                if (info.length() > 0) {
////                    System.out.println(name + " " + info);
////                }
////            } while (!info.equals("Matched."));
//
//
////            br.close();
////            pw.close();
////            socket.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//    }
//
//
//}
