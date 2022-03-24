package lox;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
//import Scanner;
//Lox解释器 教程地址：https://github.com/GuoYaxiang/craftinginterpreters_zh/blob/main/content/4.%E6%89%AB%E6%8F%8F.md#user-content-fnref-8-a4f417a088c0766f8053b647539309ca
public class Lox
{
    static boolean hadError = false;
    public static void main(String[] args) throws IOException
    {
        if(args.length >1)
        {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }
        else if (args.length == 1)
        {
            runFile(args[0]);
        }
        else
        {
            runPrompt();
        }
    }
    private static void runFile(String path) throws IOException 
    {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError){System.exit(65);};
    }
    private static void runPrompt() throws IOException
    {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for(;;)
        {
            System.out.print("> ");
            String line = reader.readLine();
            if(line == null)
            {
                break;
            }
            run(line);
            hadError = false;
        }
    }
    private static void run(String source)
    {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        //print tokens
        for(Token token : tokens)
        {
            System.out.println(token);
        }
    }
    //错误报告（报错器）
    static void error(int line, String message) 
    {
        report(line, "", message);
    }
    
    private static void report(int line, String where,String message) 
    {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}