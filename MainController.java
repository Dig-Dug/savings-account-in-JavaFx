package de.cbw.jav.account.fx;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.UnaryOperator;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;


/**
 * Controller class handling events of the main pane.
 * 
 * @author Sascha Baumeister
 */
public class MainController implements AutoCloseable {
	// Account
	final Sparbuch account;

	// the JDBC database connection, the user email, and the user password
	private final Connection jdbcConnection;

	// the pane controlled by a controller instance
	private final BorderPane pane;

	// children of above pane that provide processing information,
	// and any child used to output information
	private final TextField accountInput;
	private final TextField operandInput;
	private final TextArea dataOutput;
	private final Label errorOutput;
	private final TextArea kontonummer;


	/**
	 * Extracts the children of the main pane into instance variables, and registers
	 * the event handlers for the former. @param mainPane the main pane @param
	 * jdbcConnection the JDBC connection @throws NullPointerException if any of the
	 * given arguments is {@code null} @throws SQLException if there is an SQL
	 * related problem @throws
	 */
	public MainController(final BorderPane mainPane, final Connection jdbcConnection) throws IOException, SQLException {
		this.account = new Sparbuch(0.05);
		this.jdbcConnection = Objects.requireNonNull(jdbcConnection);

		// initialize view
		this.pane = Objects.requireNonNull(mainPane);

		// store the children of the pane above into instance variables
		final HBox topPane = (HBox) this.pane.getTop();
		final VBox leftPane = (VBox) this.pane.getLeft();
		this.accountInput = (TextField) topPane.getChildren().get(0);
		this.operandInput = (TextField) leftPane.getChildren().get(0);
		this.dataOutput = (TextArea) this.pane.getCenter();
		this.kontonummer = (TextArea) this.pane.getCenter();
		this.errorOutput = (Label) this.pane.getBottom();

		final UnaryOperator<Change> integerFilter = change -> change.getText().trim().matches("^[+-]?[0-9]*(\\.[0-9]*)?$") ? change : null;
		this.operandInput.setTextFormatter(new TextFormatter<>(integerFilter));

		final Button saveButton = (Button) topPane.getChildren().get(1);
		final Button loadButton = (Button) topPane.getChildren().get(2);
		final Button depositButton = (Button) leftPane.getChildren().get(1);
		final Button withdrawButton = (Button) leftPane.getChildren().get(2);
		final Button interestButton = (Button) leftPane.getChildren().get(3);
		final Button predictButton = (Button) leftPane.getChildren().get(4);
		final Button displayButton = (Button) leftPane.getChildren().get(5);

		// register event handlers
		saveButton.setOnAction(event -> this.handleSaveButtonAction());
		saveButton.setOnTouchPressed(event -> this.handleSaveButtonAction());
		loadButton.setOnAction(event -> this.handleLoadButtonAction());
		loadButton.setOnTouchPressed(event -> this.handleLoadButtonAction());
		displayButton.setOnAction(event -> this.handleDisplayButtonAction());
		displayButton.setOnTouchPressed(event -> this.handleDisplayButtonAction());
		depositButton.setOnAction(event -> this.handleDepositButtonAction());
		depositButton.setOnTouchPressed(event -> this.handleDepositButtonAction());
		withdrawButton.setOnAction(event -> this.handleWithdrawButtonAction());
		withdrawButton.setOnTouchPressed(event -> this.handleWithdrawButtonAction());
		interestButton.setOnAction(event -> this.handleInterestButtonAction());
		interestButton.setOnTouchPressed(event -> this.handleInterestButtonAction());
		predictButton.setOnAction(event -> this.handlePredictButtonAction());
		predictButton.setOnTouchPressed(event -> this.handlePredictButtonAction());
	}


	/**
	 * Closes the resources if this controller, for example it's JDBC database
	 * connection, files, TCP ports etc.
	 * 
	 * @throws SQLException if there is an SQL related problem
	 */
	public void close() throws SQLException {
		this.jdbcConnection.close();
	}


	/**
	 * Returns the controlled pane.
	 * 
	 * @return the controlled pane
	 */
	public BorderPane getPane() {
		return this.pane;
	}


	/**
	 * Returns the JDBC connection.
	 * 
	 * @return the JDBC connection
	 */
	public Connection getJdbcConnection () {
		return this.jdbcConnection;
	}


	protected void handleSaveButtonAction () {
		System.out.println("save button pressed");
		try {
			if (this.accountInput.getText().isEmpty()) return;
			this.account.setKontonummer(Long.parseLong(this.accountInput.getText()));

			if (!DataSources.updateSparbuch(this.jdbcConnection, this.account))
				DataSources.insertSparbuch(this.jdbcConnection, this.account);

			this.dataOutput.setText(String.format("Transaction saved: %d\n", this.account.getKontonummer()));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}


	protected void handleLoadButtonAction () {
		try {
			if (this.accountInput.getText().isEmpty()) return;

			this.account.setKontonummer(Long.parseLong(this.accountInput.getText()));
			List<Map<String, Object>> accounts = DataSources.querySparbuch(this.jdbcConnection, this.account);
			if (accounts.size() != 1)
				throw new IllegalStateException();

			final Map<String, Object> data = accounts.get(0);
			this.account.setInhaber((String) data.get("name"));
			this.account.setGuthaben((Long) data.get("balance"));
			this.account.setZinssatz((Double) data.get("interestRate"));
			this.account.setZuletztModifiziert((Long) data.get("lastModification"));

			final double balance = this.account.getGuthaben();
			this.dataOutput.setText(String.format("Accountnumber: %d\nBalance: %.2f€\n ", this.account.getKontonummer(), balance * 0.01));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}


	protected void handleDisplayButtonAction () {
		try {
			System.out.println("display button pressed");
			final long accountNumber = this.account.getKontonummer();
			final double balance = this.account.getGuthaben();
			this.dataOutput.setText(String.format("balance: %.2f€\nAccountnumber: %d\n", balance * 0.01, accountNumber));
		} catch (NumberFormatException e) {
			System.out.println("Please enter a number");
		}
	}


	protected void handleDepositButtonAction () {
		try {
			final long deposit = Math.round(100 * Double.parseDouble(this.operandInput.getText()));
			final long balance = this.account.zahleEin(deposit);
			this.dataOutput.setText(String.format("deposit: %.2f€\nbalance: %.2f€\n", deposit * 0.01, balance * 0.01));
		} catch (NumberFormatException e) {
			System.out.println("Please enter a number");
			this.dataOutput.setText("please enter a number");
		}
	}


	protected void handleWithdrawButtonAction () {
		try {
			final long withdraw = Math.round(100 * Double.parseDouble(this.operandInput.getText()));
			final long balance = this.account.hebeAb(withdraw);
			this.dataOutput.setText(String.format("withdraw: %.2f€\nbalance: %.2f€\n", withdraw * 0.01, balance * 0.01));
		} catch (NumberFormatException e) {
			System.out.println("Please enter a number");

		}
	}


	protected void handleInterestButtonAction () {
		final long balance = this.account.verzinse();
		this.dataOutput.setText(String.format("yearly interest => %.2f€\n", balance * 0.01));
	}


	protected void handlePredictButtonAction () {
		try {
			final int days = Integer.parseInt(this.operandInput.getText());
			final long balance = this.account.getErtrag(days);
			this.dataOutput.setText(String.format("dayly interest over %d days\nbalance: %.2f€\n", days, balance * 0.01));
		} catch (NumberFormatException e) {
			System.out.println("Please enter a number");
		}
	}
}