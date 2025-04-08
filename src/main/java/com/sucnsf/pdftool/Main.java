package com.sucnsf.pdftool;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

public class Main extends Application {
    private static String preDirPath = "";
    private static String preOutPath = "";
    private static File configFile = null;
    private TextArea logTextArea = new TextArea();
    public static void main(String[] args) throws IOException {
        String filePath = Paths.get(System.getProperty("java.io.tmpdir"), "merge-pdf.conf").toString();
        configFile = new File(filePath);
        if (!configFile.exists()) {
            configFile.createNewFile();
            List<String> lines = Arrays.asList("preDirPath=", "preOutPath=");
            FileUtil.writeLines(lines, configFile, CharsetUtil.CHARSET_UTF_8);
        } else {
            List<String> lines = FileUtil.readUtf8Lines(configFile);
            for (String line : lines) {
                if (line.startsWith("preDirPath=")) {
                    preDirPath = line.replaceAll("preDirPath=", "").trim();
                } else if (line.startsWith("preOutPath=")) {
                    preOutPath = line.replaceAll("preOutPath=", "").trim();
                }
            }
        }

        Application.launch(args);
    }

    public static void mergePDFs(List<String> filePaths, String outputPath) {
        try {
            PDFMergerUtility mergerUtility = new PDFMergerUtility();
            mergerUtility.setDestinationFileName(outputPath);
            for (String filePath : filePaths) {
                mergerUtility.addSource(new File(filePath));
            }
            mergerUtility.mergeDocuments(null); // 这里传入null，因为我们已经在addSource中添加了所有文件
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox root = new VBox();
        root.getChildren().add(new Label("PDF合并工具，默认输出文件为：merged_年月日时分秒.pdf"));
        TextField dirPathTextField = new TextField(preDirPath);
        dirPathTextField.setPromptText("双击选择目录");
        dirPathTextField.setPrefWidth(500d);
        dirPathTextField.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("选择文件夹");
                File file = directoryChooser.showDialog(primaryStage);
                if (file != null) {
                    try {
                        dirPathTextField.setText(file.getCanonicalPath());
                    } catch (IOException ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("文件路径错误：" + ex.getMessage());
                        alert.showAndWait();
                    }
                }
            }

        });
        TextField outPathTextField = new TextField(preOutPath);
        outPathTextField.setPromptText("双击选择目录");
        outPathTextField.setPrefWidth(500d);
        outPathTextField.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("选择文件夹");
                File file = directoryChooser.showDialog(primaryStage);
                if (file != null) {
                    try {
                        outPathTextField.setText(file.getCanonicalPath());
                    } catch (IOException ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("文件路径错误：" + ex.getMessage());
                        alert.showAndWait();
                    }
                }
            }

        });
        // 输入表单
        FlowPane inputForm = new FlowPane();
        inputForm.setPadding(new Insets(5d, 10d, 5d, 10d));
        inputForm.setHgap(5);
        inputForm.setVgap(5);
        inputForm.getChildren().addAll(
                new Label("文件目录："),
                dirPathTextField
        );
        root.getChildren().add(inputForm);


        inputForm = new FlowPane();
        inputForm.setPadding(new Insets(5d, 10d, 5d, 10d));
        inputForm.setHgap(5);
        inputForm.setVgap(5);
        inputForm.getChildren().addAll(
                new Label("输出目录："),
                outPathTextField
        );
        root.getChildren().add(inputForm);

        Button mergeButton = new Button("合并");
        mergeButton.setOnMouseClicked(event -> {

            File file = new File(dirPathTextField.getText());

            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        boolean flag = pathname.getName().toLowerCase().endsWith(".pdf");
                        print("文件："+pathname.getName()+"，是否PDF文件："+flag);
                        return flag;
                    }
                });
                if (files != null) {
                    print("共找到" + files.length + "个PDF文件");
                    List<String> list = new ArrayList<>();
                    for (File f : files) {
                        try {
                            list.add(f.getCanonicalPath());
                        } catch (IOException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setContentText("文件路径错误：" + e.getMessage());
                            alert.showAndWait();
                        }
                    }
                    print("原始PDF数量" + files.length + "个，加载PDF数量为" + list.size() + "个");
                    if (list.size() == files.length) {
                        String outPath = Paths.get(outPathTextField.getText(), "merged_"+cn.hutool.core.date.DateUtil.format(new Date(),"yyyyMMddHHmmss")+".pdf").toString();
                        print("开始合并PDF");
                        mergePDFs(list, outPath);
                        print("合并完成，文件路径：" + outPath);
                        List<String> lines = Arrays.asList("preDirPath=" + dirPathTextField.getText(), "preOutPath=" + outPathTextField.getText());
                        FileUtil.writeLines(lines, configFile, CharsetUtil.CHARSET_UTF_8);
                        print("配置路径保存成功");

                    }

                }
            } else {
                print("文件不存在");
            }

        });
        inputForm = new FlowPane();
        inputForm.setPadding(new Insets(5d, 10d, 5d, 10d));
        inputForm.setHgap(5);
        inputForm.setVgap(5);
        inputForm.getChildren().addAll(
                mergeButton
        );
        root.getChildren().add(inputForm);

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(5d, 10d, 5d, 10d));
        Button clearButton = new Button("清空");
        clearButton.setOnMouseClicked(event -> {
            logTextArea.setText("");
        });
        vBox.getChildren().addAll(
                clearButton,
                logTextArea
        );
        root.getChildren().add(vBox);
        primaryStage.setScene(new Scene(root, 700d, 500d));
        primaryStage.show();
    }
    private void print(String log){
        logTextArea.setText(logTextArea.getText()  + "\n"+ log);
    }
}