package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.FontMetrics;

import datastructures.User;
import datastructures.Product;

//*/
// Aristotle Daskaleas - 2026 : "User Recommendation Database With Bipartite Graphing" 
// This class stores methods allowing a visual graph to be generated.
//*/

//*/
// This class should NOT be instantiated as only it's public static method should be called.
//*/

public class Graph {
    private Graph(){};

    // method for displaying the graph
    public static void showGraph(List<User> users, List<Product> products) {
        final List<User> finalUsers = List.copyOf(users);
        final List<Product> finalProducts = List.copyOf(products);

        SwingUtilities.invokeLater(() -> {
            // closeable window independent from rest of program
            JFrame frame = new JFrame("Product Recommendation Graph");

            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            frame.setContentPane(new GraphPanel(finalUsers, finalProducts));

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.setResizable(false);
        });

        System.out.println("Graph generated. Check other window.");
    }


    // actual graph rendering
    private static class GraphPanel extends JPanel {
        private Map<Integer, Integer> userPositions = new HashMap<>();
        private Map<Integer, Integer> productPositions = new HashMap<>();

        private static final Map<Integer, List<Product>> recommendedProducts = new HashMap<>();
        private static final Map<Integer, List<Product>> usedProducts = new HashMap<>();

        private static final int BOTTOM = 660;
        private static final int NODE_SPACING = 25;

        private final List<User> users;
        private final List<Product> products;

        private static final Color USER_COLOR = new Color(219, 234, 254);
        private static final Color PRODUCT_COLOR = new Color(220, 252, 231);
        private static final Color USED_COLOR = new Color(22, 163, 74);
        private static final Color RECOMMENDED_COLOR = new Color(217, 119, 6);

        private static final BasicStroke USED_STROKE = new BasicStroke(2.0f);
        private static final BasicStroke RECOMMENDED_STROKE = new BasicStroke(
            2.0f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            10.0f,
            new float[]{8.0f, 6.0f},
            0.0f
        );

        private static final int NODE_WIDTH = 220;
        private static final int LEFT_X = 45;
        private static final int TOP = 75;

        public GraphPanel(List<User> users, List<Product> products) {
            this.users = List.copyOf(users);
            this.products = List.copyOf(products);

            // calculate node positions
            this.userPositions = calculatePositions(
                this.users,
                User::getUserID,
                TOP,
                BOTTOM
            );

            this.productPositions = calculatePositions(
                this.products,
                Product::product_id,
                TOP,
                BOTTOM
            );

            // cache user-product relationships
            for (User user : this.users) {
                recommendedProducts.put(
                    user.getUserID(),
                    List.copyOf(user.getRecommendedProducts())
                );

                usedProducts.put(
                    user.getUserID(),
                    List.copyOf(user.getUsedProducts())
                );
            }

            setPreferredSize(new Dimension(1600, 1000));
            setBackground(Color.WHITE);
        }


        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();

            try {
                // antialiasing
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int rightX = getWidth() - 45 - NODE_WIDTH;
                int bottom = getHeight() - 40;

                // determine node positions
                //Map<Integer, Integer> userPositions = calculatePositions(users, User::getUserID, TOP, bottom);
                //Map<Integer, Integer> productPositions = calculatePositions(products, Product::product_id, TOP, bottom);

                // adjust node height based on the number of nodes
                int maximumNodes = Math.max(users.size(), products.size());
                double spacing = (double) (bottom - TOP) / (maximumNodes + 1);
                int nodeHeight = Math.max(18, Math.min(34, (int) (spacing * 0.8)));

                // draw column headings
                g.setColor(Color.BLACK);
                g.setFont(new Font("SansSerif", Font.BOLD, 18));

                g.drawString("              USERS", LEFT_X, 45);
                g.drawString("         PRODUCTS", rightX, 45);

                // draw legend
                drawLegend(g);

                // draw edges FIRST so nodes appear above them
                for (User user : users) {
                    int userY = userPositions.get(user.getUserID());

                    // Draw recommended products
                    for (Product product : recommendedProducts.get(user.getUserID())) {
                        Integer productY = productPositions.get(product.product_id());
                        if (productY == null) continue;

                        g.setColor(RECOMMENDED_COLOR);
                        g.setStroke(RECOMMENDED_STROKE);
                        g.drawLine(
                            LEFT_X + NODE_WIDTH,
                            userY,
                            rightX,
                            productY
                        );
                    }

                    // Draw used products
                    for (Product product : usedProducts.get(user.getUserID())) {
                        Integer productY = productPositions.get(product.product_id());
                        if (productY == null) continue;

                        g.setColor(USED_COLOR);
                        g.setStroke(USED_STROKE);
                        g.drawLine(
                            LEFT_X + NODE_WIDTH,
                            userY,
                            rightX,
                            productY
                        );
                    }
                }

                // draw user nodes
                for (User user : users) {
                    drawNode(
                        g,
                        LEFT_X,
                        userPositions.get(user.getUserID()),
                        NODE_WIDTH,
                        nodeHeight,
                        user.getName(),
                        USER_COLOR
                    );
                }

                // draw product nodes
                for (Product product : products) {
                    drawNode(
                        g,
                        rightX,
                        productPositions.get(product.product_id()),
                        NODE_WIDTH,
                        nodeHeight,
                        product.name(),
                        PRODUCT_COLOR
                    );
                }
            } finally {
                g.dispose(); // close window without terminating program
            }
        }

        // calculate evenly distributed vertical positions
        private static <T> Map<Integer, Integer> calculatePositions(
            List<T> nodes,
            ToIntFunction<? super T> idExtractor,
            int top,
            int bottom
        ) {
            Map<Integer, Integer> positions = new HashMap<>();
            final int count = nodes.size();
            if (count <= 0) return positions;

            int availableHeight = bottom - top;
            int spacing = Math.max(NODE_SPACING, availableHeight / (count + 1));

            for (int i = 0; i < count; i++) {
                int id = idExtractor.applyAsInt(nodes.get(i));
                int y = top + spacing * (i + 1);

                if (positions.put(id, y) != null) {
                    throw new IllegalArgumentException("Duplicate graph node ID: " + id);
                }
            }

            return positions;
        }

        // draw individual nodes
        private void drawNode(
            Graphics2D g,
            int x,
            int centerY,
            int width,
            int height,
            String name,
            Color fill
        ) {
            int y = centerY - height / 2;

            g.setColor(fill);
            g.fillRoundRect(x, y, width, height, 12, 12);

            g.setColor(Color.DARK_GRAY);
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(x, y, width, height, 12, 12);

            // fit long names inside the node
            g.setFont(new Font("SansSerif", Font.PLAIN, 13));

            FontMetrics metrics = g.getFontMetrics();
            String displayedName = name;

            // remove last character from name until it fits properly in node
            while (!displayedName.isEmpty() && (metrics.stringWidth(displayedName) > width - 16)) {
                displayedName = displayedName.substring(0, displayedName.length() - 1);
            }

            if (!displayedName.equals(name)) displayedName += "...";

            int textWidth = metrics.stringWidth(displayedName);
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();

            g.setColor(Color.BLACK);
            g.drawString(displayedName, textX, textY);
        }

        // draw edge legend
        private void drawLegend(Graphics2D g) {
            int x = getWidth() / 2 - 110;
            int y = 70;

            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(USED_COLOR);
            g.setStroke(USED_STROKE);

            g.drawLine(x, y, x + 35, y);
            g.drawString("Used", x + 42, y + 4);

            x += 110;
            g.setColor(RECOMMENDED_COLOR);
            g.setStroke(RECOMMENDED_STROKE);

            g.drawLine(x, y, x + 35, y);
            g.drawString("Recommended", x + 42, y + 4);
        }
    }
}