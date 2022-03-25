package com.lox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import com.lox.Token;
//import lox.TokenType.*;
import static com.lox.TokenType.*;
//行扫描器
public class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  //start和current字段是指向字符串的偏移量。start字段指向被扫描的词素中的第一个字符，current字段指向当前正在处理的字符。line字段跟踪的是current所在的源文件行数，这样我们产生的标记就可以知道其位置。
  private int start = 0;
  private int current = 0;
  private int line = 1;
  private static final Map<String, TokenType> keywords;
  //标识符定义
  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    keywords.put("class",  CLASS);
    keywords.put("else",   ELSE);
    keywords.put("false",  FALSE);
    keywords.put("for",    FOR);
    keywords.put("fun",    FUN);
    keywords.put("if",     IF);
    keywords.put("nil",    NIL);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);
    keywords.put("return", RETURN);
    keywords.put("super",  SUPER);
    keywords.put("this",   THIS);
    keywords.put("true",   TRUE);
    keywords.put("var",    VAR);
    keywords.put("while",  WHILE);
  }
  public Scanner(String source) 
  {
    this.source = source;
  }

  public List<Token> scanTokens() 
  {
    while (!isAtEnd())
    {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }
  //然后，我们还有一个辅助函数，用来告诉我们是否已消费完所有字符。
  private boolean isAtEnd()
  {
    return current >= source.length();
  }
  //这里不使用正则的方法，而是想鳄鱼一样吃进去每个字符并返回给匹配处理程序进行匹配，如果匹配成功，则拉出来规范化的token
  //advance()方法获取源文件中的下一个字符并返回它。advance()用于处理输入，addToken()则用于输出。该方法获取当前词素的文本并为其创建一个新标记。我们马上会使用另一个重载方法来处理带有字面值的标记。
  private char advance() {
    current++;
    return source.charAt(current - 1);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
  private void scanToken() 
  {
    char c = advance();
    switch (c) {
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break;
      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      case '/':
        if (match('/')) 
        {
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(SLASH);
        }
        break;
      //忽略无用字符
      case ' ':
      case '\r':
      case '\t':
          // Ignore whitespace.
          break;
  
      case '\n':
          line++;
          break;
      //处理字面量
      case '"': string(); break;
      default:
        // 替换部分开始
        if (isDigit(c))
        {
          number();
          // 新增部分开始
        } else if (isAlpha(c))
        {
          //标识符
          identifier();
        // 新增部分结束

        } else {
          Lox.error(line, "Unexpected character.");
        }
        // 替换部分结束
        break;
    }
  }
  private void number() 
  {
    while (isDigit(peek())) advance();

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance();

      while (isDigit(peek())) advance();
    }

    addToken(NUMBER,
        Double.parseDouble(source.substring(start, current)));
  }
  //与注释类似，我们会一直消费字符，直到"结束该字符串。如果输入内容耗尽，我们也会进行优雅的处理，并报告一个对应的错误。
  private void string() 
  {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    // The closing ".
    advance();

    // Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }
  //这就像一个有条件的advance()。只有当前字符是我们正在寻找的字符时，我们才会消费。
  //使用match()，我们分两个阶段识别这些词素。例如，当我们得到 !时，我们会跳转到它的case分支。这意味着我们知道这个词素是以 !开始的。然后，我们查看下一个字符，以确认词素是一个 != 还是仅仅是一个 !。
  private boolean match(char expected) 
  {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }
  private char peek()
  {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }
  private boolean isDigit(char c)
  {
    return c >= '0' && c <= '9';
  }
  private char peekNext()
  {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }
  //字幕
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }
  //标识符 如果匹配的话，就使用关键字的标记类型。否则，就是一个普通的用户定义的标识符。
  private void identifier() {
    while (isAlphaNumeric(peek())) advance();
    // 替换部分开始
    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null) type = IDENTIFIER;
    addToken(type);
    // 替换部分结束
  }
}