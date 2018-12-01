package ru.serbis.mnvp.rpc.lexer;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private char peek = ' ';
    private StringReader reader;
    private boolean aval = true;
    private boolean notNext = false;

    public Lexer(String input) {
        reader = new StringReader(input);
    }

    private void readch() {
        int r;
        try {
            if ((r = reader.read()) != -1){
                peek = (char) r;
                aval= true;
            } else {
                aval = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Token> getList() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            Token token = scan();
            if (token == null)
                return tokens;

            tokens.add(token);
        }
    }

    public Token scan() {
        while (true) {
            if (!notNext)
                readch();
            else
                notNext = false;
            if (!aval)
                return null;
            if (peek == ' ' || peek == '\t') continue;
            else break;
        }

        switch (peek) {
            case ':':
                return new Token(Tag.COLON, ":");
            case '(':
                return new Token(Tag.LBRACKET, "(");
            case ')':
                return new Token(Tag.RBRACKET, ")");
            case ',':
                return new Token(Tag.COMMA, ",");
        }
        if (Character.isDigit(peek)) {
            long v = 0;
            do {
                v = 10 * v + Character.digit(peek, 10);
                readch();
            } while (Character.isDigit(peek));
            notNext =true;
            if (peek != '.') {
                if (v >= Integer.MAX_VALUE)
                    return new Token(Tag.LONG, String.valueOf(v));
                else
                    return new Token(Tag.INT, String.valueOf(v));
            }
            double x = v;
            double d = 10;
            for (;;) {
                readch();
                if (!Character.isDigit(peek)) break;
                x = x + Character.digit(peek, 10) / d;
                d = d * 10;
            }
            notNext = true;
            String xInStr = String.valueOf(x);
            if (xInStr.length() - 1 - xInStr.indexOf(".") > 7)
                return new Token(Tag.DOUBLE, String.valueOf(xInStr));
            else
                return new Token(Tag.FLOAT, String.valueOf(xInStr));
        }

        if (Character.isLetter(peek)) {
            StringBuffer b = new StringBuffer();
            do {
                b.append(peek);
                readch();
                if (!aval)
                    break;
            } while (Character.isLetterOrDigit(peek));
            String s = b.toString();
            //peek = ' ';
            notNext = true;
            switch (s) {
                case "true":
                    return new Token(Tag.BOOL, s);
                case "false":
                    return new Token(Tag.BOOL, s);
                default:
                    return new Token(Tag.ID, s);
            }


        }

        if (peek == '"') {
            readch();
            StringBuffer b = new StringBuffer();
            do {
                b.append(peek);
                readch();
                if (!aval)
                    return null;
            } while (peek != '"');

            return new Token(Tag.STRING, b.toString());
        }

        return null;
    }
}