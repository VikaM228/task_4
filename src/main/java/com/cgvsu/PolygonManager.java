package com.cgvsu;

import javafx.scene.control.Button;
import javafx.scene.shape.Polygon;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class PolygonManager {
    public AnchorPane modelPane; // Используем уже существующий AnchorPane
    private List<Polygon> polygons = new ArrayList<>(); // Список полигонов

    // Метод для удаления последнего полигона
    public void delpolygon(ActionEvent actionEvent) {
        if (!polygons.isEmpty()) {
            // Удаляем последний добавленный полигон
            Polygon polygonToRemove = polygons.get(polygons.size() - 1);

            // Удаляем полигон с панели
            modelPane.getChildren().remove(polygonToRemove);

            // Удаляем полигон из списка
            polygons.remove(polygonToRemove);
        } else {
            System.out.println("Нет полигонов для удаления!");
        }
    }

    // Метод для добавления нового полигона
    public void addPolygon(double... points) {
        Polygon polygon = new Polygon(points);
        polygon.setFill(Color.LIGHTBLUE); // Задаём цвет
        polygon.setStroke(Color.BLACK);  // Контур полигона

        polygons.add(polygon);
        modelPane.getChildren().add(polygon);
    }
}
