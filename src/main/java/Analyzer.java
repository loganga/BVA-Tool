import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.*;

/**
 * The driver for the BVA tool. Takes a filepath and a method name as parameters.
 */
class Analyzer {

    public static void main(String[] args) {
        FileReader in = null;
        try {
            analyze(new FileInputStream(args[0]), args[1]);

        } catch (Exception e) {
            System.out.println("Either the filepath or method name was invalid.");
        }
    }

    /**
     * This method acts as a driver for running the visitor pattern of the JavaParser library
     * and generates the table based on the visitor's results.
     */
    static void analyze(InputStream in, String methodName) throws Exception {
        CompilationUnit cu;

        try {
            cu = JavaParser.parse(in);
        } finally {
            in.close();
        }
        MethodVisitor visitor = new MethodVisitor(methodName);
        visitor.visit(cu, null);

        TableGenerator tg = new TableGenerator();
        tg.generateTable(visitor.getBoundaryValues(), visitor.getBoundaryValueTypes());
    }
}
