package com.example.praka_6;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private EditText titleText, descriptionText;
    private Button addButton, updateButton, deleteButton, chooseImageButton;
    private ListView listView;
    private ImageView productImageView;
    private Uri selectedImageUri;
    private ArrayAdapter<String> adapter;
    private String selectedProductTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация библиотеки Paper для хранения данных
        Paper.init(this);

        // Связываем элементы интерфейса
        titleText = findViewById(R.id.titleText);
        descriptionText = findViewById(R.id.descriptionText);
        addButton = findViewById(R.id.addButton);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        listView = findViewById(R.id.listView);
        productImageView = findViewById(R.id.productImageView);

        // Адаптер для отображения списка продуктов
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getProductTitles());
        listView.setAdapter(adapter);

        // Обработчик выбора элемента из списка
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedProductTitle = adapter.getItem(position);

            // Получаем данные выбранного продукта
            Product product = Paper.book().read(selectedProductTitle, null);

            if (product != null) {
                titleText.setText(product.getTitle());
                descriptionText.setText(product.getDescription());
                productImageView.setImageURI(Uri.parse(product.getImagePath()));
                selectedImageUri = Uri.parse(product.getImagePath());
            }
        });

        // Обработчик кнопки выбора изображения
        chooseImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        });

        // Обработчик кнопки добавления продукта
        addButton.setOnClickListener(v -> {
            String title = titleText.getText().toString();
            String description = descriptionText.getText().toString();

            if (!title.isEmpty() && !description.isEmpty() && selectedImageUri != null) {
                Product product = new Product(title, description, selectedImageUri.toString());
                Paper.book().write(title, product);
                updateProductList();
                clearInputs();
            } else {
                Toast.makeText(MainActivity.this, "Заполните все поля и выберите изображение", Toast.LENGTH_SHORT).show();
            }
        });

        // Обработчик кнопки обновления продукта
        updateButton.setOnClickListener(v -> {
            if (selectedProductTitle == null) {
                Toast.makeText(MainActivity.this, "Пожалуйста, выберите товар", Toast.LENGTH_SHORT).show();
                return;
            }

            // Получаем текущий продукт из хранилища
            Product currentProduct = Paper.book().read(selectedProductTitle);

            if (currentProduct == null) {
                Toast.makeText(MainActivity.this, "Ошибка: товар не найден", Toast.LENGTH_SHORT).show();
                return;
            }

            // Используем текущие данные, если поля не заполнены
            String newTitle = titleText.getText().toString().isEmpty() ? currentProduct.getTitle() : titleText.getText().toString();
            String newDescription = descriptionText.getText().toString().isEmpty() ? currentProduct.getDescription() : descriptionText.getText().toString();
            String newImagePath = (selectedImageUri != null) ? selectedImageUri.toString() : currentProduct.getImagePath();

            // Удаляем старую запись и создаем новую
            Paper.book().delete(selectedProductTitle);
            Product updatedProduct = new Product(newTitle, newDescription, newImagePath);
            Paper.book().write(newTitle, updatedProduct);

            updateProductList();
            clearInputs();

            Toast.makeText(MainActivity.this, "Товар обновлен!", Toast.LENGTH_SHORT).show();
        });

        // Обработчик кнопки удаления продукта
        deleteButton.setOnClickListener(v -> {
            if (selectedProductTitle == null) {
                Toast.makeText(MainActivity.this, "Пожалуйста, выберите товар", Toast.LENGTH_SHORT).show();
                return;
            }

            Paper.book().delete(selectedProductTitle);
            updateProductList();
            clearInputs();
        });
    }

    // Метод обновления списка продуктов
    private void updateProductList() {
        adapter.clear();
        adapter.addAll(getProductTitles());
        adapter.notifyDataSetChanged();
    }

    // Получение списка названий продуктов
    private List<String> getProductTitles() {
        return new ArrayList<>(Paper.book().getAllKeys());
    }

    // Очистка полей ввода
    private void clearInputs() {
        titleText.setText("");
        descriptionText.setText("");
        selectedImageUri = null;
        productImageView.setImageURI(null);
        selectedProductTitle = null;
    }

    // Обработка результата выбора изображения
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            selectedImageUri = data.getData();
            productImageView.setImageURI(selectedImageUri);
        }
    }
}
