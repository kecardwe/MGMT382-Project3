package com.example.mgmt382;

import java.net.*;
import java.io.*;

public class Server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(new ServerThread(clientSocket));
                thread.start();
                System.out.println("Client connected");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class ServerThread implements Runnable {
    private Socket clientSocket;
    public static final Object obj = new Object();

    public ServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            InputStream inputStream = clientSocket.getInputStream();
            DataInputStream in = new DataInputStream(inputStream);
            OutputStream outputStream = clientSocket.getOutputStream();
            DataOutputStream out = new DataOutputStream(outputStream);
            Seller seller = null;
            Customer customer = null;
            Seller toSeller;
            Customer toCustomer;
            Messager messager = null;
            boolean isSeller = false;
            while (true) {
                String inputLine = in.readUTF();
                boolean response;
                String[] inputContents = inputLine.split(";");
                String method = inputContents[0];
                String parameter = inputContents[1];
                if (method.equals("verifyEmail")) {
                    synchronized(obj) {
                        response = verifyEmail(parameter);
                    }
                    out.writeBoolean(response);
                    out.flush();
                } else if (method.equals("verifyPassword")) {
                    String[] parameters = parameter.split(",");
                    synchronized(obj) {
                        response = verifyPassword(parameters[0], parameters[1]);
                    }
                    out.writeBoolean(response);
                    out.flush();
                } else if (method.equals("addSeller")) {
                    String[] parameters = parameter.split(",");
                    synchronized(obj) {
                        addSeller(parameters[0], parameters[1], parameters[2]);
                    }
                } else if (method.equals("addCustomer")) {
                    String[] parameters = parameter.split(",");
                    synchronized(obj) {
                        addCustomer(parameters[0], parameters[1]);
                    }
                } else if (method.equals("checkSell")) {
                    synchronized(obj) {
                        response = checkSell(parameter);
                    }
                    out.writeBoolean(response);
                    out.flush();
                } else if (method.equals("checkSell1")) {
                    synchronized(obj) {
                        isSeller = checkSell(parameter);
                    }
                    out.writeBoolean(isSeller);
                    out.flush();
                } else if (method.equals("makeUser")) {
                    if (isSeller) {
                        synchronized(obj) {
                            seller = new Seller(parameter);
                        }
                    } else {
                        synchronized(obj) {
                            customer = new Customer(parameter);
                        }
                    }
                } else if (method.equals("block")) {
                    String[] parameters = parameter.split(",");
                    boolean isInvisible = Boolean.parseBoolean(parameters[0]);
                    if (isSeller) {
                        synchronized(obj) {
                            toCustomer = new Customer(parameters[1]);
                            seller.block(toCustomer, isInvisible);
                        }
                    } else {
                        synchronized(obj) {
                            toSeller = new Seller(parameters[1]);
                            customer.block(toSeller, isInvisible);
                        }
                    }
                } else if (method.equals("checkStore")) {
                    synchronized(obj) {
                        response = checkStore(parameter);
                    }
                    out.writeBoolean(response);
                    out.flush();
                } else if (method.equals("browse")) {
                    if (isSeller) {
                        String[] customers;
                        synchronized(obj) {
                            customers = seller.listCustomers();
                        }
                        out.writeInt(customers.length);
                        out.flush();
                        for (String cust : customers) {
                            out.writeUTF(cust);
                            out.flush();
                        }
                        String[] blockedBy;
                        synchronized(obj) {
                            blockedBy = seller.getBlockedEmails();
                        }
                        out.writeInt(blockedBy.length);
                        out.flush();
                        for (String email : blockedBy) {
                            out.writeUTF(email);
                            out.flush();
                        }
                    } else {
                        String[] stores;
                        synchronized(obj) {
                            stores = customer.listStores();
                        }
                        out.writeInt(stores.length);
                        out.flush();
                        for (String store : stores) {
                            out.writeUTF(store);
                            out.flush();
                        }
                        String[] blockedBy;
                        synchronized(obj) {
                            blockedBy = customer.getBlockedEmails();
                        }
                        out.writeInt(blockedBy.length);
                        out.flush();
                        for (String email : blockedBy) {
                            out.writeUTF(email);
                            out.flush();
                        }
                    }
                } else if (method.equals("getAStore")) {
                    String store;
                    synchronized(obj) {
                        toSeller = new Seller(parameter);
                        store = toSeller.getStores()[0];
                    }
                    out.writeUTF(store);
                    out.flush();
                } else if (method.equals("changePassword")) {
                    if (isSeller) {
                        synchronized(obj) {
                            seller.changePassword(parameter);
                        }
                    } else {
                        synchronized(obj) {
                            customer.changePassword(parameter);
                        }
                    }
                } else if (method.equals("deleteAccount")) {
                    if (isSeller) {
                        synchronized(obj) {
                            seller.deleteAccount();
                        }
                    } else {
                        synchronized(obj) {
                            customer.deleteAccount();
                        }
                    }
                } else if (method.equals("showStats")) {
                    String[] statLines;
                    if (isSeller) {
                        synchronized(obj) {
                            statLines = seller.getStats();
                        }
                    } else {
                        synchronized(obj) {
                            statLines = customer.getStats();
                        }
                    }
                    out.writeInt(statLines.length);
                    out.flush();
                    for (String line : statLines) {
                        out.writeUTF(line);
                        out.flush();
                    }
                } else if (method.equals("newStore")) {
                    synchronized(obj) {
                        seller.addStore(parameter);
                    }
                } else if (method.equals("message")) {
                    String[] parameters = parameter.split(",");
                    if (isSeller) {
                        synchronized(obj) {
                            toCustomer = new Customer(parameters[1]);
                            messager = new Messager(seller, toCustomer, parameters[0]);
                        }
                    } else {
                        synchronized(obj) {
                            toSeller = new Seller(parameters[1]);
                            messager = new Messager(toSeller, customer, parameters[0]);
                        }
                    }

                } else if (method.equals("viewMessage")) {
                    String[] lines;
                    if (isSeller) {
                        synchronized(obj) {
                            lines = messager.viewMessageSeller();
                        }
                    } else {
                        synchronized(obj) {
                            lines = messager.viewMessageCustomer();
                        }
                    }
                    if (lines == null) {
                        out.writeInt(-1);
                    } else {
                        out.writeInt(lines.length);
                        out.flush();
                        for (String line : lines) {
                            out.writeUTF(line);
                            out.flush();
                        }
                    }
                } else if (method.equals("getStores")) {
                    String[] stores;
                    synchronized(obj) {
                        stores = seller.getStores();
                    }
                    out.writeInt(stores.length);
                    out.flush();
                    for (String store : stores) {
                        out.writeUTF(store);
                        out.flush();
                    }
                } else if (method.equals("getEmailFromStore")) {
                    String emailFromStore;
                    synchronized(obj) {
                        emailFromStore = getEmailFromStore(parameter);
                    }
                    out.writeUTF(emailFromStore);
                    out.flush();
                } else if (method.equals("sendMessage")) {
                    if (isSeller) {
                        synchronized(obj) {
                            messager.writeMessageSeller(parameter);
                        }
                    } else {
                        synchronized(obj) {
                            messager.writeMessageCustomer(parameter);
                        }
                    }
                } else if (method.equals("getMessageNum")) {
                    int messageNum;
                    if (isSeller) {
                        synchronized(obj) {
                            messageNum = messager.getMessageNumberSeller();
                        }
                    } else {
                        synchronized(obj) {
                            messageNum = messager.getMessageNumberCustomer();
                        }
                    }
                    out.writeInt(messageNum);
                    out.flush();
                } else if (method.equals("editMessage")) {
                    String[] parameters = parameter.split(",");
                    synchronized(obj) {
                        messager.editMessage(Integer.parseInt(parameters[0]), parameters[1]);
                    }
                } else if (method.equals("deleteMessage")) {
                    if (isSeller) {
                        synchronized(obj) {
                            messager.deleteMessageSeller(Integer.parseInt(parameter));
                        }
                    } else {
                        synchronized(obj) {
                            messager.deleteMessageCustomer(Integer.parseInt(parameter));
                        }
                    }
                } else if (method.equals("importTxt")) {
                    boolean badPath = false;
                    try {
                        if (isSeller) {
                            synchronized(obj) {
                                messager.importTextFileSeller(parameter);
                            }
                        } else {
                            synchronized(obj) {
                                messager.importTextFileCustomer(parameter);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        badPath = true;
                    }
                    out.writeBoolean(badPath);
                } else if (method.equals("exportAsCSV")) {
                    if (isSeller) {
                        synchronized(obj) {
                            messager.exportAsCSVSeller(parameter);
                        }
                    } else {
                        synchronized(obj) {
                            messager.exportAsCSVCustomer(parameter);
                        }
                    }
                } else if (method.equals("sortStats")) {
                    if (isSeller) {
                        seller.sortStats();
                    } else {
                        customer.sortStats();
                    }
                }
            }
        } catch (SocketException e) {
            if (e.getMessage().equals("Connection reset")) {
                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    public static boolean verifyEmail(String email) { //returns true if email exists in either file
        try (BufferedReader br1 = new BufferedReader(new FileReader("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\sellerLogins.txt"));
             BufferedReader br2 = new BufferedReader(new FileReader("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\customerLogins.txt"))) {
            for (String line1 = br1.readLine(); line1 != null; line1 = br1.readLine()) {
                String[] elements = line1.split(";");
                if (email.equals(elements[0])) {
                    return true;
                }
            }
            for (String line2 = br2.readLine(); line2 != null; line2 = br2.readLine()) {
                String[] elements = line2.split(";");
                if (email.equals(elements[0])) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean verifyPassword(String email, String password) {
        try (BufferedReader br1 = new BufferedReader(new FileReader("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\sellerLogins.txt"));
             BufferedReader br2 = new BufferedReader(new FileReader("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\customerLogins.txt"))) {
            for (String line1 = br1.readLine(); line1 != null; line1 = br1.readLine()) {
                String[] elements = line1.split(";");
                if (elements[0].equals(email)) {
                    return elements[1].equals(password);
                }
            }
            for (String line2 = br2.readLine(); line2 != null; line2 = br2.readLine()) {
                String[] elements = line2.split(";");
                if (elements[0].equals(email)) {
                    return elements[1].equals(password);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    private static boolean checkStore(String store) {
        boolean isStore = false;
        try (BufferedReader br1 = new BufferedReader(new FileReader("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\sellerLogins.txt"))) {
            for (String line1 = br1.readLine(); line1 != null; line1 = br1.readLine()) {
                if (line1.contains(store)) {
                    isStore = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isStore;
    }
    private static String getEmailFromStore(String store) {
        String emailFromStore = null;
        try (BufferedReader br1 = new BufferedReader(new FileReader("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\sellerLogins.txt"))) {
            for (String line = br1.readLine(); line != null; line = br1.readLine()) {
                if (line.contains(store)) {
                    String[] elements = line.split(";");
                    emailFromStore = elements[0];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return emailFromStore;
    }
    private static void addCustomer(String email, String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\customerLogins.txt", true))) {
            writer.newLine();
            writer.write(email + ";" + password + ";");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addSeller(String email, String password, String store) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\sellerLogins.txt", true))) {
            writer.newLine();
            writer.write(email + ";" + password + ";;" + store);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkSell(String email) {
        boolean type = false;
        try (BufferedReader br1 = new BufferedReader(new FileReader("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\sellerLogins.txt"));
             BufferedReader br2 = new BufferedReader(new FileReader("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\customerLogins.txt"))) {
            for (String line1 = br1.readLine(); line1 != null; line1 = br1.readLine()) {
                String[] elements = line1.split(";");
                if (elements[0].equals(email)) {
                    type = true;
                }
            }
            for (String line2 = br2.readLine(); line2 != null; line2 = br2.readLine()) {
                String[] elements = line2.split(";");
                if (elements[0].equals(email)) {
                    type = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return type;
    }
}