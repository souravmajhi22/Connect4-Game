package sample;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
public class Main extends Application {
    Stage window;
    int red=0,yellow=0;
    private static final int TILE_SIZE = 80;
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;

    private boolean redMove = true;
    private Disc[][] grid = new Disc[COLUMNS][ROWS];

    private Pane discRoot = new Pane();
    /**
     * createContent method of type Parent.
     * This method creates the root of the GUI platform.
     *
     * @return [root]
     */
    private Parent createContent() {
        Pane root = new Pane();
        root.getChildren().add(discRoot);

        Shape gridShape = makeGrid();
        root.getChildren().add(gridShape);
        root.getChildren().addAll(makeColumns());

        return root;
    }
    /**
     * makeGrid method of type Shape.
     * This method create the grid of the Connect4 GUI. Its rectangular shape/form is defined,
     * the shape of the circles, their centers, its dimension, and the surface's color.
     *
     * @return [shape] [shape of the Connect4 GUI is returned]
     */
    private Shape makeGrid() {
        Shape shape = new Rectangle((COLUMNS + 1) * TILE_SIZE, (ROWS + 1) * TILE_SIZE);

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                //Setting the radius of the circle
                Circle circle = new Circle(TILE_SIZE / 2);
                //Setting the center of the circle
                circle.setCenterX(TILE_SIZE / 2);
                circle.setCenterY(TILE_SIZE / 2);
                circle.setTranslateX(x * (TILE_SIZE + 5) + TILE_SIZE / 4);
                circle.setTranslateY(y * (TILE_SIZE + 5) + TILE_SIZE / 4);

                shape = Shape.subtract(shape, circle);
            }
        }
        //Instantiating the Light.Distant class
        Light.Distant light = new Light.Distant();
        //Setting the properties of the light source
        light.setAzimuth(45.0);
        light.setElevation(30.0);

        //Instantiating the Lighting class
        Lighting lighting = new Lighting();
        //Setting the source of the light
        lighting.setLight(light);
        lighting.setSurfaceScale(5.0);

        shape.setFill(Color.BLUE);
        shape.setEffect(lighting);

        return shape;
    }
    /**
     * Generic method makeColumns. This method creates and
     * sort the empty circle spaces in columns. It control/set
     * the mouse events on the GUI platform.
     *
     * @return [list]
     */
    private List<Rectangle> makeColumns() {
        //New Array created
        List<Rectangle> list = new ArrayList<>();

        for (int x = 0; x < COLUMNS; x++) {
            Rectangle rect = new Rectangle(TILE_SIZE, (ROWS + 1) * TILE_SIZE);
            rect.setTranslateX(x * (TILE_SIZE + 5) + TILE_SIZE / 4);
            rect.setFill(Color.TRANSPARENT);

            rect.setOnMouseEntered(e -> rect.setFill(Color.rgb(200, 200, 50, 0.3)));
            rect.setOnMouseExited(e -> rect.setFill(Color.TRANSPARENT));

            final int column = x;
            rect.setOnMouseClicked(e -> placeDisc(new Disc(redMove), column));

            list.add(rect);
        }

        return list;
    }
    /**
     * placeDisc method takes two parameters.
     * First parameter is disc of type Disc and the second is
     * column of type int. This method controls the position/placement
     * of the disc (or player position) on the GUI platform.
     * It controls the animation on the GUI platform.
     *
     * @param disc
     * @param column
     */
    private void placeDisc(Disc disc, int column) {
        int row = ROWS - 1;
        do {
            if (!getDisc(column, row).isPresent())
                break;

            row--;
        } while (row >= 0);

        if (row < 0)
            return;

        grid[column][row] = disc;
        discRoot.getChildren().add(disc);
        disc.setTranslateX(column * (TILE_SIZE + 5) + TILE_SIZE / 4);

        final int currentRow = row;

        TranslateTransition animation = new TranslateTransition(Duration.seconds(0.5), disc);
        animation.setToY(row * (TILE_SIZE + 5) + TILE_SIZE / 4);
        animation.setOnFinished(e -> {
            if (gameEnded(column, currentRow))
            {
                gameOver();
            }

            redMove = !redMove;
        });
        animation.play();
    }
    /**
     * gameEnded method of type boolean.
     * This method has two arguments of type int, column and row.
     * It checks if the player has or hasn't aligned 4 discs to win the game.
     *
     * @param column
     * @param row
     *
     * @return [true/False] [return the position in which the player won.]
     */
    private boolean gameEnded(int column, int row) {
        List<Point2D> vertical = IntStream.rangeClosed(row - 3, row + 3)
                .mapToObj(r -> new Point2D(column, r))
                .collect(Collectors.toList());

        List<Point2D> horizontal = IntStream.rangeClosed(column - 3, column + 3)
                .mapToObj(c -> new Point2D(c, row))
                .collect(Collectors.toList());

        Point2D topLeft = new Point2D(column - 3, row - 3);
        List<Point2D> diagonal1 = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> topLeft.add(i, i))
                .collect(Collectors.toList());

        Point2D botLeft = new Point2D(column - 3, row + 3);
        List<Point2D> diagonal2 = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> botLeft.add(i, -i))
                .collect(Collectors.toList());

        return checkRange(vertical) || checkRange(horizontal) || checkRange(diagonal1) || checkRange(diagonal2);
    }
    /**
     * checkRange method of type boolean.
     * This method as a single parameter of a generic list type, points.
     * This method check the position of the disc, and counts if they are
     * placed in a chain to allow the player to win.
     *
     * @param points
     *
     * @return [true/false]
     */
    private boolean checkRange(List<Point2D> points) {
        int chain = 0;

        for (Point2D p : points) {
            int column = (int) p.getX();
            int row = (int) p.getY();

            Disc disc = getDisc(column, row).orElse(new Disc(!redMove));
            if (disc.red == redMove) {
                chain++;
                if (chain == 4) {
                    return true;
                }
            } else {
                chain = 0;
            }
        }

        return false;
    }
    /**
     * gameOver method.
     * This method displays the winner each time that a player aligns 4 discs.
     */
    Label l=new Label();

    private void gameOver()
    {
      //  l.setText("Winner: " + (redMove ? "RED" : "YELLOW"));



        System.out.println("Winner: " + (redMove ? "RED" : "YELLOW"));


      /*  if(redMove) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("RED IS THE WINNER");
            alert.showAndWait();
        }
        else {

           Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("YELLOW IS THE WINNER");
            alert.showAndWait();
        }*/
    }
    /**
     * getDisc method of generic type Optional<Disc>.
     * This method has two arguments of type int, column and row.
     * This method checks if the grid is empty or filled.
     *
     * @param column
     * @param row
     *
     * @return [status of the grid]
     */
    private Optional<Disc> getDisc(int column, int row) {
        if (column < 0 || column >= COLUMNS || row < 0 || row >= ROWS)
            return Optional.empty();

        return Optional.ofNullable(grid[column][row]);
    }
    /**
     * inner class Disc.
     */
    private static class Disc extends Circle {
        /**
         * Red variable declared.
         */
        private final boolean red;
        /**
         * constructor Disc.
         * This constructor has one parameter/argument of red of type boolean.
         *
         * @param red
         */
        public Disc(boolean red) {
            super(TILE_SIZE / 2, red ? Color.RED : Color.YELLOW);
            this.red = red;

            setCenterX(TILE_SIZE / 2);
            setCenterY(TILE_SIZE / 2);
        }
    }
    /**
     * start method.
     * Beginning of the GUI of connect4 Game.
     *
     * @param stage
     */
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.show();
        window = stage;
        window.setTitle("Connect4");
    }
    /**
     * Main method.
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}