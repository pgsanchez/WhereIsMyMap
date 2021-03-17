package com.pgsanchez.whereismymap.presentation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.pgsanchez.whereismymap.R;
import com.pgsanchez.whereismymap.use_cases.UseCaseDB;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    UseCaseDB useCaseDB;

    private GoogleApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        useCaseDB = new UseCaseDB(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.export_option:
                exportar();
                break;
            case R.id.import_option:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /* Comprobar a qué activity respondemos
        *  Posibles respuestas:
        *      1. Nuevo mapa + resultado ok
        *      2. Nuevo mapa + resultado cancel
        */
    }

    /**
     * Función a la que se llama cuando se pulsa el botón "Nuevo Mapa".
     * Llamará al formulario de crear nuevo mapa
     * @param view
     */
    public void onNewMap(View view) {
        Intent intent;
        intent = new Intent(this, NewMapActivity.class);
        startActivity(intent);
    }

    /**
     * Función a la que se llama cuando se pulsa el botón de "Buscar Mapa"
     * @param view
     */
    public void onMapsList(View view){
        EditText edtTextToFind = findViewById(R.id.edtTextToFind);
        // Se llama a la ventana del listado de mapas con el texto que se desea buscar. Puede ser null.
        Intent intent;
        intent = new Intent(this, MapsListActivity.class);
        intent.putExtra("name", edtTextToFind.getText().toString());
        startActivity(intent);
    }

    public void onBtnNumFiles(View view) {
        TextView tvNumFiles = findViewById(R.id.tvNumFiles);
        //Defino la ruta donde busco los ficheros
        File f = ((Aplication) getApplication()).imgsPath;
        //Creo el array de tipo File con el contenido de la carpeta
        File[] files = f.listFiles();

        tvNumFiles.setText(Integer.toString(files.length));
    }

    public void onBtnGMap(View view) {
        // Mostrar activity de google maps
        Intent intent;
        intent = new Intent(this, GMapsActivity.class);
        startActivity(intent);
    }


    private void exportar(){
        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .build();

        // Crear una carpeta ("whereismymap" es el nombre de la carpeta):
        MetadataChangeSet changeSet =
                new MetadataChangeSet.Builder()
                        .setTitle("whereismymap")
                        .build();
        // Obtenemos una referencia de la localización donde vamos a crear la carpeta. En este caso, la carpeta raíz.
        DriveFolder folder = Drive.DriveApi.getRootFolder(apiClient);

        folder.createFolder(apiClient, changeSet).setResultCallback(
                new ResultCallback<DriveFolder.DriveFolderResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFolderResult result) {
                        if (result.getStatus().isSuccess())
                            Log.i("exportar", "Carpeta creada con ID = " + result.getDriveFolder().getDriveId());
                        else
                            Log.e("exportar", "Error al crear carpeta");
                    }
                });


        // crear fichero
        Drive.DriveApi.newDriveContents(apiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (result.getStatus().isSuccess()) {

                            writeSampleText(result.getDriveContents());

                            MetadataChangeSet changeSet =
                                    new MetadataChangeSet.Builder()
                                            .setTitle("ficheroNuevo")
                                            .setMimeType("text/plain")
                                            .build();

                            //Opción 1: Directorio raíz
                            DriveFolder folder = Drive.DriveApi.getRootFolder(apiClient);

                            //Opción 2: Otra carpeta distinta al directorio raiz
                            //DriveFolder folder =
                            //    DriveId.decodeFromString("DriveId:CAESABjKGSD6wKnM7lQoAQ==").asDriveFolder();

                            folder.createFile(apiClient, changeSet, result.getDriveContents())
                                    .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                        @Override
                                        public void onResult(DriveFolder.DriveFileResult result) {
                                            if (result.getStatus().isSuccess()) {
                                                Log.i("exportar", "Fichero creado con ID = " + result.getDriveFile().getDriveId());
                                            } else {
                                                Log.e("exportar", "Error al crear el fichero");
                                            }
                                        }
                                    });
                        } else {
                            Log.e("exportar", "Error al crear DriveContents");
                        }
                    }
                });


    }

    private void writeSampleText(DriveContents driveContents) {
        OutputStream outputStream = driveContents.getOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);

        try {
            writer.write("Esto es un texto de prueba!");
            writer.close();
        } catch (IOException e) {
            Log.e("writeSampleText", "Error al escribir en el fichero: " + e.getMessage());
        }
    }

    private void copyImage(DriveContents driveContents){
        //Defino la ruta donde busco los ficheros
        File f = ((Aplication) getApplication()).imgsPath;
        //Creo el array de tipo File con el contenido de la carpeta
        File[] files = f.listFiles();
        String nameFile = files[0].getName();



        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .build();


        // Obtenemos una referencia de la localización donde vamos a crear la carpeta. En este caso, la carpeta raíz.
        DriveFolder folder = Drive.DriveApi.getRootFolder(apiClient);



        // write content to DriveContents
        OutputStream outputStream = driveContents.getOutputStream();
        // Write the bitmap data from it.
        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setMimeType("image/jpeg").setTitle(nameFile)
                .build();
        Bitmap image = BitmapFactory.decodeFile(f + "/" + nameFile);
        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 80, bitmapStream);
        try {
            outputStream.write(bitmapStream.toByteArray());
        } catch (IOException e1) {
            Log.i("E", "Unable to write file contents.");
        }
        image.recycle();
        outputStream = null;
        String title = "noisy";

        Log.i("E", "Creating new pic on Drive (" + title + ")");
        folder.createFile(apiClient, metadataChangeSet, driveContents)
                .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFileResult result) {
                        if (result.getStatus().isSuccess()) {
                            Log.i("exportar", "Imagen creada con ID = " + result.getDriveFile().getDriveId());
                        } else {
                            Log.e("exportar", "Error al crear la imagen");
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("MainActivity:", "OnConnectionFailed: " + connectionResult);
    }
}