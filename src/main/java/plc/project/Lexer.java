package plc.project;

import java.util.ArrayList;
import java.util.List;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are * helpers you need to use, they will make the implementation a lot easier. */
public final class Lexer {

    private final CharStream chars;

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {
        List<Token> listOfTokens = new ArrayList<>();
        while(chars.has(0)){
            if(!match(" ")){
                listOfTokens.add(lexToken());
            }
            if(!match("\b")){
                listOfTokens.add(lexToken());
            }
            if(!match("\n")){
                listOfTokens.add(lexToken());
            }
            if(!match("\r")){
                listOfTokens.add(lexToken());
            }
            if(!match("\t")){
                listOfTokens.add(lexToken());
            }
            if(match(" ")){
                chars.skip();
            }
            if(match("\b")){
                chars.skip();
            }
            if(match("\n")){
                chars.skip();
            }
            if(match("\r")){
                chars.skip();
            }
            if(match("\t")){
                chars.skip();
            }
        }

        return listOfTokens;
      //  throw new UnsupportedOperationException(); //TODO
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     *
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     */
    public Token lexToken() {
        //Delegate to a specific type.
        // Grammar:
        String numbers = "[\\+-]?|(0)[1-9][0-9]*";
        String secondNumbers = "(0)?|(-)?[1-9][0-9]*";
        String numberSigns = "[\\+-]";
        String identifiers = "((@)|[A-Za-z])[A-Za-z0-9_]*";
        String operators = "[!=] '='? | '&&' | '||'";
        String strings = "(\")([^\"\\n\\r\\\\]|(\\)[bnrt\"\\\\]))*(\")";
        String secondStrings ="[^\"\\n\\r\\\\]" ;
        String thirdStrings = "[bnrt\"\\\\]*";

        String characters = "[^'\\n\\r\\\\]";
        String secondCharacters = "[bnrt'\"\\\\]";
        String escapes = "\\[bnrt'\"\\\\]";


        if(((peek("@") || peek("[A-Za-z]")) && peek("[A-Za-z0-9_]*"))){
            return lexIdentifier();
        } else if(peek(numbers) || peek(secondNumbers)){
            return lexNumber();
        } else if(peek("\"") && (peek(secondStrings) || peek(thirdStrings)) && peek("\"")){
            return lexString();
        } else if(peek("[']") && (peek(characters) || peek(secondCharacters)) && peek("[']")){
            return lexCharacter();
        } else if(peek(escapes) == true){
            //return lexEscape();
        } else {
            return lexOperator();
        }
        throw new UnsupportedOperationException(); //TODO
    }

    public Token lexIdentifier() {
        System.out.println("Identifier");
        return chars.emit(Token.Type.IDENTIFIER);
        //throw new UnsupportedOperationException(); //TODO
    }

    public Token lexNumber() {
        System.out.println("Numbersito!");
        if(peek("[\\+-]")){
            match("[\\+-]");
        }
        while(peek("[1-9]*")){
            match("[0-9]");
            if(peek("[.]")){
                match("\\.");
                if(peek("[0-9]*")) {
                    match("[0-9]");
                    return chars.emit(Token.Type.DECIMAL);
                }
         }
        }
        return chars.emit(Token.Type.INTEGER);
    }

    public Token lexCharacter() {
        System.out.println("Character!");
        return chars.emit(Token.Type.CHARACTER);
        //throw new UnsupportedOperationException(); //TODO
    }

    public Token lexString() {
        System.out.println("String!");
        return chars.emit(Token.Type.STRING);
        //throw new UnsupportedOperationException(); //TODO
    }

    public void lexEscape() {
        throw new UnsupportedOperationException(); //TODO
    }

    public Token lexOperator() {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) {
        // String... implements a variable list of arguments. Similar to
        // an array, but is not statically-constructed.

        for (int i = 0; i < patterns.length; i++) {
            // We do not iterate over the index positions of the first string at index 0 inside of patterns.
            // If we have more characters, go to the right side of the statement.
            if (!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i])) {
                // No match.
                return false;
            }

        }
        return true;

        //throw new UnsupportedOperationException(); //TODO (in Lecture)
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns) {
        boolean peek = peek(patterns);

        if(peek == true){
            for(int i = 0; i < patterns.length; i++){
                chars.advance();
            }
        }
        //throw new UnsupportedOperationException(); //TODO (in Lecture)
        return peek;
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     *
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

    }

}
