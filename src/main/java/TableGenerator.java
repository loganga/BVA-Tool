import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Generates the table of results for the BVA Tool.
 */
public class TableGenerator {

    /**
     * Creates and displays a table based on the given HashMaps of input parameters
     * and their respective boundary values.
     *
     * @param boundaryValues The map of inputs to their boundary values.
     * @param boundaryValueTypes The map of inputs to their types.
     */
    public void generateTable(HashMap<String, List<Object>> boundaryValues, HashMap<String, Class> boundaryValueTypes) {
        Set<String> inputs = boundaryValues.keySet();
        int rowCount = 0;

        for (String s : inputs) {
            rowCount += boundaryValues.get(s).size();
        }

        Object[] columns = inputs.toArray();
        Object[][] rowData = new String[rowCount][inputs.size()];
        // Fill rows with blank data
        for (int i = 0; i < rowData.length; i++) {
            for (int j = 0; j < rowData[i].length; j++) {
                rowData[i][j] = "-";
            }
        }

        JTable table = new JTable(rowData, columns);
        int rowIndex = 0;
        for (int i = 0; i < columns.length; i++) {
            List<Object> results = boundaryValues.get(columns[i]);
            for (int j = 0; j < results.size(); j++) {
                Class type = boundaryValueTypes.get(columns[i]);
                table.setValueAt(type.cast(results.get(j)).toString(), rowIndex + j, i);
            }
            rowIndex += results.size();
        }

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(500, 300));

        JFrame frame = new JFrame("BVA Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(tableScrollPane);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
}
}
