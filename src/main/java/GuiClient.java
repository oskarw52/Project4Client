
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Node;
import javafx.util.Duration;

public class GuiClient extends Application{

	Stage stage; //easier scene switching
	HashMap<String, Scene> sceneMap; //scene switching
	Client clientConnection;


	//menu screen
	Label gameTitle;
	Button onlineGameButton;
	Button offlineGameButton;
	//menu screen

	//matchmaking screen
	Label matchmakingLabel;
	//matchmaking screen

	//gameplay screen
	Button quitGame; //for server testing
	Label gameStatus;
	Label playerTurn;
	GridPane yourGrid;
	GridPane enemyGrid;
	ComboBox row1;
	ComboBox row2;
	ComboBox col1;
	ComboBox col2;
	Button placeShip;
	ArrayList<Integer> shipSizes;
	int shipIndex;
	boolean yourTurn = false;
	Label enemyShipsLeft;

	VBox vbox1;  //server testing
	HBox hbox1;  //server testing
	HBox hbox2; //server testing
	HBox hbox3; //server testing

	//gameplay screen
	Button mainMenu;
	Label resultLabel;

	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		clientConnection = new Client(data->{
				Platform.runLater(()->{
					GameInfo gameData = (GameInfo)data;
					if (gameData.gameFound) {
						stage.setScene(sceneMap.get("gameplay"));
						gameStatus.setText("Place ship size: " + shipSizes.get(shipIndex)); //Arraylist (2, 3, 3, 4, 5)
					} else if (gameData.placeShip) {
						if (gameData.validPlacement) {
							if (gameData.allShipsPlaced) {
								gameStatus.setText("All ships placed");
								//get rid of ship placement items on screen
								hbox3.setVisible(false);
								hbox3.setDisable(true);
							}
							else {
								//update next ship to place
								shipIndex++;
								gameStatus.setText("Place ship size: " + shipSizes.get(shipIndex));
							}
							//on server grid is from 0 to 9  and on gui its from 1 to 10
							int gridRow1 = gameData.r1 + 1;
							int gridCol1 = gameData.c1 + 1;
							int gridRow2 = gameData.r2 + 1;
							int gridCol2 = gameData.c2 + 1;
							//they will contain list of cells to color when placing a ship
							ArrayList<Integer> gridRows = new ArrayList<>();
							ArrayList<Integer> gridCols = new ArrayList<>();
							if (gridRow2 - gridRow1 == 0) {
								for (int i = Math.min(gridCol1, gridCol2); i <= Math.max(gridCol1, gridCol2); i++) {
									gridCols.add(i);
									gridRows.add(gridRow1);
								}
							}
							else {
								for (int i = Math.min(gridRow1, gridRow2); i <= Math.max(gridRow1, gridRow2); i++) {
									gridCols.add(gridCol2);
									gridRows.add(i);
								}
							}
							//debugging
							System.out.println("Cords to color"); {
								for (int i = 0; i < gridRows.size(); i++) {
									System.out.println(gridRows.get(i) + " " + gridCols.get(i));
								}
							}
							//coloring all cells that have their cords in cords arrays
							for(int i = 0; i < gridRows.size(); i++) {
								for (Node n : yourGrid.getChildren()) {
									if (GridPane.getRowIndex(n).equals(gridRows.get(i)) && GridPane.getColumnIndex(n).equals(gridCols.get(i))) {
										n.setStyle("-fx-background-color: black; -fx-opacity: 1");
									}
								}
							}
						}
						else {
							gameStatus.setText("Invalid cords");
						}
						placeShip.setDisable(false); //allow for next placement
					}
					//when both players placed their ships server will send it to both players
					//one message will have yourturn true and the other false
					else if (gameData.allShipsPlacedBothPlayers) {
						gameStatus.setText("starting game");
						yourTurn = gameData.yourTurn;
						playerTurn.setVisible(true);
						enemyShipsLeft.setVisible(true);

						if (yourTurn) {
							playerTurn.setText("Your turn");
						}
						else {
							playerTurn.setText("Enemy turn");
						}

					}
					//When yourturn is true you can click on enemy grid
					//when its false the event is doing nothing
					else if (gameData.youHitShip) {
						int gridRow = gameData.hitShipRow + 1;
						int gridCol = gameData.hitShipCol + 1;
						for (Node n : enemyGrid.getChildren()) {
							if (GridPane.getRowIndex(n) == gridRow && GridPane.getColumnIndex(n) == gridCol) {
							//if the current node is the one you shot
								//update the color based on status
								//update the message based on status
								if (gameData.shipHitResult == 0) { //miss
									gameStatus.setText("You missed");
									((Button) n).setStyle("-fx-background-color: white; -fx-opacity: 1");
								}
								else {
									((Button) n).setStyle("-fx-background-color: red; -fx-opacity: 1");
									if (gameData.shipHitResult == 1) { //hit
										gameStatus.setText("Nice hit");
									} else if (gameData.shipHitResult == 2) { //destruction
										gameStatus.setText("Ship destroyed");
										enemyShipsLeft.setText("Enemy ships left: " + gameData.shipsLeftAfterHit);
										if (gameData.shipsLeftAfterHit == 0) {  //final ship destroyed
											stage.setScene(sceneMap.get("win"));
										}
									}
									else { //repeat shot
										gameStatus.setText("You already shot there");
									}
								}
							}
						}
						playerTurn.setText("Enemy turn");
					} else if (gameData.yourShipWasHit) {
						yourTurn = true;
						playerTurn.setText("Your turn");
						int gridRow = gameData.hitShipRow + 1;
						int gridCol = gameData.hitShipCol + 1;
						//same as above
						for (Node n : yourGrid.getChildren()) {
							if (GridPane.getRowIndex(n) == gridRow && GridPane.getColumnIndex(n) == gridCol) {
								if (gameData.shipHitResult == 0) {
									((Button) n).setStyle("-fx-background-color: white; -fx-opacity: 1");
									gameStatus.setText("Enemy missed");
								} else {
									((Button) n).setStyle("-fx-background-color: red; -fx-opacity: 1");
									gameStatus.setText("Enemy hit your ship");
								}
							}
						}
						//if the oponent doesnt have any ships anymore
						if (gameData.shipsLeftAfterHit == 0) {
							stage.setScene(sceneMap.get("lose"));
						}
					}


				});
		});
							
		clientConnection.start();
		//IMPORTANT scene switching/now if you want a button to change scene you can
		//declare that in the scene creation function
		stage = primaryStage;
		sceneMap = new HashMap<String, Scene>();
		sceneMap.put("mainMenu",  generateMainMenu());
		sceneMap.put("matchmaking", generateMatchmaking());
		sceneMap.put("gameplay", generateGameplayScene());
		sceneMap.put("win", generateResultScreen("YOU WON"));
		sceneMap.put("lose", generateResultScreen("YOU LOST"));



		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		primaryStage.setScene(sceneMap.get("mainMenu"));
		primaryStage.setTitle("Battleships");
		primaryStage.show();
	}

	public Scene generateMainMenu() {
		StackPane pane = new StackPane();
		ImageView i1 = new ImageView(new Image("/images/ship.png"));
		i1.setPreserveRatio(true);
		i1.setFitWidth(407);
		ImageView i2 = new ImageView(new Image("/images/ship.png"));
		i2.setPreserveRatio(true);
		i2.setFitWidth(407);
		gameTitle = new Label("BATTLE SHIP");
		gameTitle.getStyleClass().add("title");
		onlineGameButton = new Button("Online");
		onlineGameButton.getStyleClass().add("button");
		offlineGameButton = new Button("Offline");
		offlineGameButton.getStyleClass().add("button");
		VBox v1 = new VBox(30, onlineGameButton, offlineGameButton);
		v1.setAlignment(Pos.CENTER);
		VBox v2 = new VBox(90, gameTitle, v1);
		v2.setAlignment(Pos.CENTER);
		pane.getChildren().addAll(i1,v2,i2);
		StackPane.setAlignment(i1, Pos.BOTTOM_LEFT);
		StackPane.setAlignment(v2, Pos.CENTER_RIGHT);
		StackPane.setAlignment(i2, Pos.BOTTOM_RIGHT);

		onlineGameButton.setOnAction(e->{
			stage.setScene(sceneMap.get("matchmaking"));
			GameInfo data = new GameInfo();
			data.lookingForGame = true;
			clientConnection.sendInfo(data);

		});
		Scene scene = new Scene(pane, 1440,1024);
		scene.getStylesheets().add("/styles/style1.css");
		return scene;
	}

	public Scene generateMatchmaking() {
		gameTitle = new Label("BATTLE SHIP");
		gameTitle.getStyleClass().add("title");
		matchmakingLabel = new Label("Matchmaking...");
		matchmakingLabel.getStyleClass().add("result");
		animate(matchmakingLabel);

		VBox v1 = new VBox(90, gameTitle, matchmakingLabel);
		v1.setAlignment(Pos.TOP_CENTER);
		v1.setPadding(new Insets(200));
		Scene scene = new Scene(v1, 1440, 1024);
		scene.getStylesheets().add("/styles/style1.css");
		return scene;
	}

	// this function is made for matchmakingLabel's animation
	private void animate(Label label) {
		Timeline timeline = new Timeline(
				new KeyFrame(Duration.seconds(0.5), e -> label.setText("Matchmaking")),
				new KeyFrame(Duration.seconds(1), e -> label.setText("Matchmaking.")),
				new KeyFrame(Duration.seconds(1.5), e -> label.setText("Matchmaking..")),
				new KeyFrame(Duration.seconds(2), e -> label.setText("Matchmaking..."))
		);
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
	}

	public Scene generateGameplayScene() {
		gameTitle = new Label("BATTLE SHIP");
		gameTitle.getStyleClass().add("title");
		quitGame = new Button("Quit");
		gameStatus = new Label();
		playerTurn = new Label();
		enemyShipsLeft = new Label("Enemy ships left: 5");
		enemyShipsLeft.setVisible(false);
		playerTurn.setVisible(false);
		row1 = new ComboBox<>();
		row2 = new ComboBox<>();
		col1 = new ComboBox<>();
		col2 = new ComboBox<>();
		shipIndex = 0;
		shipSizes = new ArrayList<>();
		shipSizes.add(2);
		shipSizes.add(3);
		shipSizes.add(3);
		shipSizes.add(4);
		shipSizes.add(5);
		placeShip = new Button("Place ship");
		yourGrid = new GridPane();
		yourGrid.setHgap(2); //horizontal spacing between elements in the grid
		yourGrid.setVgap(2); //vertical spacing between elements in the grid
		for (int i = 0; i < 11; i++) {
			for (int j = 0; j < 11; j++) {
				if (i == 0) {
					Label l = new Label(String.valueOf(j));  //dont want clickable
					l.setMinWidth(50);
					l.setMaxWidth(50);
					l.setMinHeight(50);
					l.setMaxHeight(50);
					l.setAlignment(Pos.CENTER);
					l.setStyle("-fx-border-color: black; -fx-border-width: 1px");
					yourGrid.add(l, j, i);
					if (j != 0) {
						col1.getItems().add(String.valueOf(j));
						col2.getItems().add(String.valueOf(j));
					}
				}
				else if (j == 0) {
					Label l = new Label(String.valueOf((char)(i + 64)));  //same as above
					l.setMinWidth(50);
					l.setMaxWidth(50);
					l.setMinHeight(50);
					l.setMaxHeight(50);
					l.setAlignment(Pos.CENTER);
					l.setStyle("-fx-border-color: black; -fx-border-width: 1px");
					yourGrid.add(l, j, i);
					row1.getItems().add(String.valueOf((char)(i + 64))); //convert to letters
					row2.getItems().add(String.valueOf((char)(i + 64)));
				}
				else {
					Button b = new Button();
					b.setDisable(true);
					b.setStyle("-fx-opacity: 1; -fx-background-color: rgb(173, 216, 230)");
					b.setMinWidth(50);
					b.setMaxWidth(50);
					b.setMinHeight(50);
					b.setMaxHeight(50);
					yourGrid.add(b, j, i);
				}
			}
		}
		yourGrid.getChildren().get(0).setVisible(false);

		enemyGrid = new GridPane();
		enemyGrid.setHgap(2);
		enemyGrid.setVgap(2);

		//same grid for enemy
		for (int i = 0; i < 11; i++) {
			for (int j = 0; j < 11; j++) {
				if (i == 0) {
					Label l = new Label(String.valueOf(j));
					l.setMinWidth(50);
					l.setMaxWidth(50);
					l.setMinHeight(50);
					l.setMaxHeight(50);
					l.setAlignment(Pos.CENTER);
					l.setStyle("-fx-border-color: black; -fx-border-width: 1px");
					enemyGrid.add(l, j, i);

				}
				else if (j == 0) {
					Label l = new Label(String.valueOf((char)(i + 64)));
					l.setMinWidth(50);
					l.setMaxWidth(50);
					l.setMinHeight(50);
					l.setMaxHeight(50);
					l.setAlignment(Pos.CENTER);
					l.setStyle("-fx-border-color: black; -fx-border-width: 1px");
					enemyGrid.add(l, j, i);
				}
				else {
					Button b = new Button();
					b.setMinWidth(50);
					b.setMaxWidth(50);
					b.setMinHeight(50);
					b.setMaxHeight(50);
					b.setStyle("-fx-background-color: rgb(173, 216, 230)");
					enemyGrid.add(b, j, i);
					b.setOnAction(e->{ //shotting enemy ship
						if (yourTurn) {
							yourTurn = false;
							int row = GridPane.getRowIndex(b) - 1;
							int col = GridPane.getColumnIndex(b) - 1;
							System.out.println("Row" + row + " col" + col);

							GameInfo hitMessage = new GameInfo();
							hitMessage.hitShip = true;
							hitMessage.hitShipRow = row;
							hitMessage.hitShipCol = col;
							clientConnection.sendInfo(hitMessage);

						}
					});

				}
			}
		}
		enemyGrid.getChildren().get(0).setVisible(false);

		hbox1 = new HBox(15, yourGrid, enemyGrid);
		hbox2 = new HBox(15, quitGame, gameStatus, playerTurn, enemyShipsLeft);
		hbox3 = new HBox(10, row1, col1, row2, col2, placeShip);
		vbox1 = new VBox(10, hbox1, hbox2, hbox3);
		//arrangements of items


		placeShip.setOnAction(e->{
			String r1 = String.valueOf(row1.getValue());
			String c1 = String.valueOf(col1.getValue());
			String r2 = String.valueOf(row2.getValue());
			String c2 = String.valueOf(col2.getValue());

			//elements in combobox are strings for better customization on gui
			//"" because we convert to string
			if (!Objects.equals(r1, "null") && !Objects.equals(c1, "null") && !Objects.equals(r2, "null") && !Objects.equals(c2, "null")) {
				placeShip.setDisable(true);  //to not spam/not have synchronization issues

				int intr1 = (int)r1.charAt(0) - 65;
				int intc1 = Integer.parseInt(c1) - 1;
				int intr2 = (int)r2.charAt(0) - 65;
				int intc2 = Integer.parseInt(c2) - 1;
				//convert into format that works with placeship function : rows and cols from 0 to 9
				GameInfo shipPlacement = new GameInfo();
				shipPlacement.placeShip = true;
				shipPlacement.r1 = intr1;
				shipPlacement.c1 = intc1;
				shipPlacement.r2 = intr2;
				shipPlacement.c2 = intc2;
				clientConnection.sendInfo(shipPlacement);
			}

		});

		//maybe add a notification to other player that this player quit
		quitGame.setOnAction(e->{
			stage.setScene(sceneMap.get("mainMenu"));
			sceneMap.put("gameplay", generateGameplayScene());
			yourTurn = false;
		});

		Scene scene = new Scene(vbox1, 1440, 1024);
		scene.getStylesheets().add("/styles/style1.css");
		return scene;
	}

	public Scene generateResultScreen(String result) {
		gameTitle = new Label("BATTLE SHIP");
		gameTitle.getStyleClass().add("title");
		resultLabel = new Label(result);
		resultLabel.getStyleClass().add("result");
		mainMenu = new Button("Main menu");
		mainMenu.getStyleClass().add("button");
		VBox v1 = new VBox(18, resultLabel, mainMenu);
		mainMenu.setOnAction(e->{
			stage.setScene(sceneMap.get("mainMenu"));
			sceneMap.put("gameplay", generateGameplayScene()); //reset gameplay screen
			yourTurn = false;
		});
		VBox v2 = new VBox(60, gameTitle, v1);
		v1.setAlignment(Pos.CENTER);
		v2.setAlignment(Pos.CENTER);
		Scene scene = new Scene(v2, 1440, 1024);
		scene.getStylesheets().add("/styles/style1.css");
		return scene;
	}


}
