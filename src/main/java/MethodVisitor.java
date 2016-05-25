import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.System.exit;

/**
 * This class extends the VoidVisitorAdapter from the JavaParser framework
 * and analyzes the input parameters of a specified method. It generates
 * boundary values for parameters used in "if" statements and stores them
 * in a map as a list that is keyed with the parameter name.
 *
 * Current supported primitive types are: int, double, char
 */
public class MethodVisitor extends VoidVisitorAdapter {

    private ArrayList<String> inputs = new ArrayList<>();
    private ArrayList<String> inputTypes = new ArrayList<>();
    private HashMap<String, Class> boundaryValueTypes = new HashMap<>();
    private HashMap<String, List<Object>> boundaryValues = new HashMap<>();
    private String methodName;


    public MethodVisitor(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Visits MethodDeclarations in the JavaParser tree structure and stores
     * parameters locally for supported types.
     * @param n
     * @param arg
     */
    @Override
    public void visit(MethodDeclaration n, Object arg) {
        if (n.getName().equals(methodName)) {
            List<Parameter> params = n.getParameters();
            List<Statement> statements = n.getBody().getStmts();
            List<Node> nodes = statements.get(0).getChildrenNodes();
            for (Parameter p : params) {
                String type = p.getType().toString();
                if (type.equals("int") || type.equals("double") || type.equals("char")) {
                    inputs.add(p.getId().toString());
                    inputTypes.add(type);
                }
            }
        }
        super.visit(n, arg);
    }

    /**
     * Visits IfStmts in the JavaParser tree structure and passes the conditions to handlers if they
     * belong to the method specified by this class.
     * @param n
     * @param arg
     */
    @Override
    public void visit(IfStmt n, Object arg) {
        MethodDeclaration dec = null;
        Node parent = n;
        int tries = 0;
        while (dec == null) {
            parent = parent.getParentNode();
            try {
                dec = (MethodDeclaration) parent;
            } catch (ClassCastException e) {
                if (tries > 30) {
                    System.out.println("There was a problem trying to find conditionals within the given method.");
                    exit(1);
                }
                tries++;
            }

        }
        if (dec.getName().equals(methodName)) {
            handleConditionChildren(n.getCondition().getChildrenNodes());
        }
        super.visit(n, arg);
    }

    /**
     * Recursively gets the children of a multi-condition if statement and sends each individual condition
     * to a handler for generation of boundary values.
     * @param nodes
     */
    private void handleConditionChildren(List<Node> nodes) {
        if (!nodes.get(0).toString().contains("&&") && !nodes.get(0).toString().contains("||")) {
            for (Node s : nodes) {
                BinaryExpr b = (BinaryExpr) s;
                handleBinaryExpr(b);
            }
        } else {
            BinaryExpr b = (BinaryExpr) nodes.get(nodes.size() - 1);
            handleBinaryExpr(b);
            handleConditionChildren(nodes.get(0).getChildrenNodes());
        }
    }

    /**
     * Determines the type of inputs in a binary expression and passes the compared values to handlers that
     * generate boundary values for their specified type.
     * @param b
     */
    private void handleBinaryExpr(BinaryExpr b) {
        Expression left = b.getLeft();
        Expression right = b.getRight();
        Expression param = null;
        Expression value = null;
        int inputIndex = 0;
        for (int i = 0; i < inputs.size(); i++) {
            if (left.toString().equals(inputs.get(i))) {
                param = left;
                value = right;
                inputIndex = i;
                break;
            } else if (right.toString().equals(inputs.get(i))) {
                param = right;
                value = left;
                inputIndex = i;
                break;
            }
        }
        if (param == null)
            return;
        String type = inputTypes.get(inputIndex);
        int intValue;
        double doubleValue;
        char charValue;
        boolean booleanValue;
        BinaryExpr.Operator op = b.getOperator();
        switch (type) {
            case "int":
                intValue = Integer.parseInt(value.toString());
                determineBounds(op, inputIndex, intValue);
                break;
            case "double":
                doubleValue = Double.parseDouble(value.toString());
                determineBounds(op, inputIndex, doubleValue);
                break;
            case "char":
                charValue = value.toString().charAt(1);
                determineBounds(op, inputIndex, charValue);
                break;
        }

    }

    /**
     * Generates boundary values for the integer type.
     *
     * @param operator The operator of the binary expression that is being analyzed.
     * @param inputIndex The index of the input in the local ArrayList of inputs.
     *                   Used here because it also corresponds to the ArrayList
     *                   of input types.
     * @param value The integer to generate values for.
     */
    private void determineBounds(BinaryExpr.Operator operator, int inputIndex, int value) {
        List<Object> bounds = new ArrayList<>();
        if (boundaryValues.containsKey(inputs.get(inputIndex))) {
            bounds = boundaryValues.get(inputs.get(inputIndex));
        }
        switch (operator) {
            case equals:
                bounds.add(value); // success
                bounds.add(value + 1); // failure
                break;
            case notEquals:
                bounds.add(value + 1); // success
                bounds.add(value); // failure
                break;
            case less:
                bounds.add(value - 1); // success
                bounds.add(value); // failure
                break;
            case greater:
                bounds.add(value + 1); // success
                bounds.add(value); // failure
                break;
            case lessEquals:
                bounds.add(value); // success
                bounds.add(value + 1); // failure
                break;
            case greaterEquals:
                bounds.add(value); // success
                bounds.add(value - 1); // failure
                break;
        }
        boundaryValues.put(inputs.get(inputIndex), bounds);
        boundaryValueTypes.put(inputs.get(inputIndex), Integer.class);
    }

    /**
     * Generates boundary values for the char type. Very similar to the integer
     * type because of how internally chars are represented.
     *
     * @param operator The operator of the binary expression that is being analyzed.
     * @param inputIndex The index of the input in the local ArrayList of inputs.
     *                   Used here because it also corresponds to the ArrayList
     *                   of input types.
     * @param value The character to generate values for.
     */
    private void determineBounds(BinaryExpr.Operator operator, int inputIndex, char value) {
        List<Object> bounds = new ArrayList<>();
        if (boundaryValues.containsKey(inputs.get(inputIndex))) {
            bounds = boundaryValues.get(inputs.get(inputIndex));
        }
        switch (operator) {
            case equals:
                bounds.add(value); // success
                bounds.add((char) (value + 1)); // failure
                break;
            case notEquals:
                bounds.add((char) (value + 1)); // success
                bounds.add(value); // failure
                break;
            case less:
                bounds.add((char) (value - 1)); // success
                bounds.add(value); // failure
                break;
            case greater:
                bounds.add((char) (value + 1)); // success
                bounds.add(value); // failure
                break;
            case lessEquals:
                bounds.add(value); // success
                bounds.add((char) (value + 1)); // failure
                break;
            case greaterEquals:
                bounds.add(value); // success
                bounds.add((char) (value - 1)); // failure
                break;
        }
        boundaryValues.put(inputs.get(inputIndex), bounds);
        boundaryValueTypes.put(inputs.get(inputIndex), Character.class);
    }

    /**
     * Generates boundary values for the double type.
     *
     * @param operator The operator of the binary expression that is being analyzed.
     * @param inputIndex The index of the input in the local ArrayList of inputs.
     *                   Used here because it also corresponds to the ArrayList
     *                   of input types.
     * @param value The double to generate values for.
     */
    private void determineBounds(BinaryExpr.Operator operator, int inputIndex, double value) {
        List<Object> bounds = new ArrayList<>();
        if (boundaryValues.containsKey(inputs.get(inputIndex))) {
            bounds = boundaryValues.get(inputs.get(inputIndex));
        }
        switch (operator) {
            case equals:
                bounds.add(value); // success
                bounds.add(value + 0.01); // failure
                break;
            case notEquals:
                bounds.add(value + 0.01); // success
                bounds.add(value); // failure
                break;
            case less:
                bounds.add(value - 0.01); // success
                bounds.add(value); // failure
                break;
            case greater:
                bounds.add(value + 0.01); // success
                bounds.add(value); // failure
                break;
            case lessEquals:
                bounds.add(value); // success
                bounds.add(value + 0.01); // failure
                break;
            case greaterEquals:
                bounds.add(value); // success
                bounds.add(value - 0.01); // failure
                break;
        }
        boundaryValues.put(inputs.get(inputIndex), bounds);
        boundaryValueTypes.put(inputs.get(inputIndex), Double.class);
    }

    public HashMap<String, List<Object>> getBoundaryValues() {
        return boundaryValues;
    }

    public HashMap<String, Class> getBoundaryValueTypes() {
        return boundaryValueTypes;
    }
}
