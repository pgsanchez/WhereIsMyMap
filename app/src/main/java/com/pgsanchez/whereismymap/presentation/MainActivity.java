package com.pgsanchez.whereismymap.presentation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.pgsanchez.whereismymap.R;
import com.pgsanchez.whereismymap.domain.Map;
import com.pgsanchez.whereismymap.use_cases.UseCaseDB;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;


public class MainActivity extends AppCompatActivity {

    UseCaseDB useCaseDB;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private DriveServiceHelper mDriveServiceHelper;


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

        switch (item.getItemId()) {
            case R.id.login_option:
                login();
                break;
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
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    handleSignInResult(data);
                }
                break;

        }

        super.onActivityResult(requestCode, resultCode, data);
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

    private void login(){
        requestSignIn();
    }

    private void exportar(){
        // 1- Hacer el requestSignIn para logarse
        // requestSignIn();
        // 2- Comprobar si existe la carpeta WhereIsMyMap. Si existe, se advierte al usuario de que se tiene que borrar.
        mDriveServiceHelper.queryFiles()
                .addOnSuccessListener(fileList -> {
                    for (com.google.api.services.drive.model.File file : fileList.getFiles()) {
                        if(file.getName().equals("WhereIsMyMap"))
                            mDriveServiceHelper.deleteFolderFile(file.getId()).addOnSuccessListener(v-> Log.d(TAG, "removed file "+file.getName())).
                                    addOnFailureListener(v-> Log.d(TAG, "File was not removed: "+file.getName()));
                    }
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Unable to query files.", exception));

        // 3-   Si el usuario está de acuerdo, borrar la carpeta WhereIsMyMap
        // 4- Crear la carpeta WhereIsMyMap

        if (mDriveServiceHelper != null) {
            Log.i(TAG, "CreateFolder");
            Task<String> folderId = mDriveServiceHelper.createFolder()
                    .addOnSuccessListener(fileId -> subirFicheros(fileId))
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't create folder.", exception));

            // 5- Subir todos los ficheros de imágenes

        }


        // 6- Subir la Base de Datos

    }

    private void subirFicheros(String folderId){
        for (Map map: useCaseDB.getAllMaps()) {
            File file = new File(((Aplication) getApplication()).imgsPath + "/" + map.getImgFileName());
            mDriveServiceHelper.uploadFile(file, "image/jpeg", folderId);
            Log.e(TAG, "Subiendo fichero " + map.getImgFileName());
        }
    }

    private void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Conectado como " + googleAccount.getEmail());

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    Drive googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("WhereIsMyMap")
                                    .build();

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                })
                .addOnFailureListener(exception -> Log.e(TAG, "Imposible conectar", exception));
    }



    private void readFile(String fileId) {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Reading file " + fileId);

            /*
            mDriveServiceHelper.readFile(fileId)
                    .addOnSuccessListener(nameAndContent -> {
                        String name = nameAndContent.first;
                        String content = nameAndContent.second;

                        mFileTitleEditText.setText(name);
                        mDocContentEditText.setText(content);

                        setReadWriteMode(fileId);
                    })
                    .addOnFailureListener(exception ->
                            Log.e(TAG, "Couldn't read file.", exception));
*/
        }
    }


}