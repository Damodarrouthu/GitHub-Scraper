import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;

public class GitHubAnalyzerUi extends Application {

    private TextArea resultArea =
            new TextArea();

    private VBox dashboardBox =
            new VBox();

    private ProgressIndicator loader =
            new ProgressIndicator();

    @Override
    public void start(Stage stage) {

        TextInputDialog dialog =
                new TextInputDialog("Students");

        dialog.setTitle("Output File");

        dialog.setHeaderText(
                "Enter Excel File Name"
        );

        dialog.setContentText(
                "File Name :"
        );

        String outputFile =
                "Students.xlsx";

        if (dialog.showAndWait().isPresent()) {

            outputFile =
                    dialog.getEditor()
                            .getText()
                            .trim();

            if (!outputFile.endsWith(".xlsx")) {

                outputFile += ".xlsx";
            }
        }

        ExcelWriter.setFileName(
                outputFile
        );

        stage.setTitle(
                "GitHub Analyzer Dashboard"
        );

        Label title =
                new Label(
                        "GitHub Credibility Checker"
                );

        title.setFont(
                Font.font(34)
        );

        title.setStyle(
                "-fx-font-weight:bold;" +
                        "-fx-text-fill:#1E3A8A;"
        );

        TextField usernameField =
                new TextField();

        usernameField.setPromptText(
                "Enter GitHub Username..."
        );

        usernameField.setPrefWidth(320);

        usernameField.setStyle(
                "-fx-font-size:16px;" +
                        "-fx-background-radius:12;"
        );

        Button analyzeBtn =
                new Button("Analyze");

        analyzeBtn.setPrefWidth(170);

        analyzeBtn.setStyle(
                "-fx-background-color:#16A34A;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:16px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:12;"
        );

        Button bulkBtn =
                new Button("Bulk Analyze");

        bulkBtn.setPrefWidth(190);

        bulkBtn.setStyle(
                "-fx-background-color:#2563EB;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:16px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:12;"
        );

        loader.setVisible(false);

        resultArea.setEditable(false);

        resultArea.setWrapText(true);

        resultArea.setPrefHeight(330);

        resultArea.setStyle(
                "-fx-font-size:15px;" +
                        "-fx-background-radius:15;"
        );

        HBox buttons =
                new HBox(
                        20,
                        analyzeBtn,
                        bulkBtn
                );

        buttons.setAlignment(Pos.CENTER);

        VBox leftPanel =
                new VBox(
                        25,
                        title,
                        usernameField,
                        buttons,
                        loader,
                        resultArea
                );

        leftPanel.setAlignment(Pos.TOP_CENTER);

        leftPanel.setPadding(
                new Insets(25)
        );

        leftPanel.setPrefWidth(650);

        dashboardBox =
                createDashboard(stage);

        HBox root =
                new HBox(
                        30,
                        leftPanel,
                        dashboardBox
                );

        root.setPadding(
                new Insets(20)
        );

        root.setStyle(
                "-fx-background-color:#EEF2F7;"
        );

        analyzeBtn.setOnAction(e -> {

            String username =
                    usernameField
                            .getText()
                            .trim();

            if (username.isEmpty()) {

                resultArea.setText(
                        "Please Enter Username"
                );

                return;
            }

            loader.setVisible(true);

            new Thread(() -> {

                String output =
                        GitHubService.analyzeUser(
                                username,
                                true
                        );

                Platform.runLater(() -> {

                    loader.setVisible(false);

                    resultArea.setText(output);

                    refreshDashboard(stage);
                });

            }).start();
        });

        bulkBtn.setOnAction(e -> {

            FileChooser chooser =
                    new FileChooser();

            chooser.setTitle(
                    "Select Username File"
            );

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Text Files",
                            "*.txt"
                    )
            );

            File file =
                    chooser.showOpenDialog(stage);

            if (file == null) {

                return;
            }

            loader.setVisible(true);

            resultArea.setText(
                    "Bulk Analysis Running..."
            );

            new Thread(() -> {

                String output =
                        GitHubService
                                .analyzeUsersFromFile(
                                        file.getAbsolutePath()
                                );

                Platform.runLater(() -> {

                    loader.setVisible(false);

                    resultArea.setText(output);

                    refreshDashboard(stage);
                });

            }).start();
        });

        Scene scene =
                new Scene(root, 1650, 850);

        stage.setScene(scene);

        stage.show();
    }

    private VBox createDashboard(Stage stage) {

        VBox panel =
                new VBox(35);

        panel.setPadding(
                new Insets(25)
        );

        panel.setPrefWidth(780);

        panel.setStyle(
                "-fx-background-color:white;" +
                        "-fx-background-radius:25;"
        );

        Label heading =
                new Label(
                        "GitHub Analytics Dashboard"
                );

        heading.setStyle(
                "-fx-font-size:32px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:#312E81;"
        );

        HBox graphs =
                new HBox(
                        30,

                        createGraphCard(
                                "High",
                                "> 70 Score",
                                GitHubService.highCount,
                                "#16A34A"
                        ),

                        createGraphCard(
                                "Medium",
                                "45 - 70 Score",
                                GitHubService.mediumCount,
                                "#2563EB"
                        ),

                        createGraphCard(
                                "Low",
                                "< 45 Score",
                                GitHubService.lowCount,
                                "#FF006E"
                        ),

                        createGraphCard(
                                "Total",
                                "All Users",
                                GitHubService.totalUsers,
                                "#6B7280"
                        )
                );

        graphs.setAlignment(Pos.CENTER);

        Button saveBtn =
                new Button("Save Graph");

        Button printBtn =
                new Button("Print");

        saveBtn.setStyle(
                "-fx-background-color:#16A34A;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:15px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:10;"
        );

        printBtn.setStyle(
                "-fx-background-color:#2563EB;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:15px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:10;"
        );

        saveBtn.setOnAction(e -> {

            try {

                WritableImage image =
                        dashboardBox.snapshot(
                                null,
                                null
                        );

                FileChooser chooser =
                        new FileChooser();

                chooser.setTitle(
                        "Save Graph"
                );

                chooser.setInitialFileName(
                        "Graph.png"
                );

                File file =
                        chooser.showSaveDialog(stage);

                if (file != null) {

                    ImageIO.write(
                            SwingFXUtils.fromFXImage(
                                    image,
                                    null
                            ),
                            "png",
                            file
                    );

                    resultArea.setText(
                            "Graph Saved Successfully"
                    );
                }

            }

            catch (Exception ex) {

                ex.printStackTrace();
            }
        });

        printBtn.setOnAction(e -> {

            PrinterJob job =
                    PrinterJob.createPrinterJob();

            if (job != null &&
                    job.showPrintDialog(stage)) {

                boolean success =
                        job.printPage(
                                dashboardBox
                        );

                if (success) {

                    job.endJob();

                    resultArea.setText(
                            "Graph Printed Successfully"
                    );
                }
            }
        });

        HBox actions =
                new HBox(
                        20,
                        saveBtn,
                        printBtn
                );

        actions.setAlignment(
                Pos.CENTER_RIGHT
        );

        panel.getChildren().addAll(
                heading,
                graphs,
                actions
        );

        return panel;
    }

    private VBox createGraphCard(

            String title,
            String subtitle,
            int value,
            String color

    ) {

        ProgressBar graph =
                new ProgressBar();

        double progress = 0;

        if (GitHubService.totalUsers != 0) {

            progress =
                    (double) value /
                            GitHubService.totalUsers;
        }

        graph.setProgress(progress);

        graph.setPrefWidth(220);

        graph.setStyle(
                "-fx-accent:" + color + ";"
        );

        Label titleLabel =
                new Label(title);

        titleLabel.setStyle(
                "-fx-font-size:24px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:" + color + ";"
        );

        Label countLabel =
                new Label(
                        value + " Users"
                );

        countLabel.setStyle(
                "-fx-font-size:22px;" +
                        "-fx-font-weight:bold;"
        );

        Label sub =
                new Label(subtitle);

        sub.setStyle(
                "-fx-font-size:16px;"
        );

        VBox box =
                new VBox(
                        18,
                        titleLabel,
                        graph,
                        countLabel,
                        sub
                );

        box.setAlignment(Pos.CENTER);

        box.setPadding(
                new Insets(20)
        );

        box.setStyle(
                "-fx-background-color:#F8FAFC;" +
                        "-fx-background-radius:20;"
        );

        return box;
    }

    private void refreshDashboard(Stage stage) {

        VBox updated =
                createDashboard(stage);

        HBox root =
                (HBox) dashboardBox
                        .getParent();

        root.getChildren().set(
                1,
                updated
        );

        dashboardBox = updated;
    }

    public static void main(String[] args) {

        launch();
    }
}