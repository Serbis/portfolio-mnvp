package ru.serbis.mnvp.rpc.lexer;

public class Token {
    public Tag tag;
    public String lexeme;

    public Token(Tag tag, String lexeme) {
        this.tag = tag;
        this.lexeme = lexeme;
    }
    public String toString() {
        return  lexeme;
    }
}