package com.example.pruebafinalis;

import android.content.Intent;
import android.text.InputType;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

public class PantallaLogin extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private ImageButton showPasswordButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
        showPasswordButton = findViewById(R.id.showPasswordButton); // Modificación

        sessionManager = new SessionManager(this);

        // Verificar si hay una sesión activa
        if (sessionManager.getAuthToken() != null) {
            // Iniciar la actividad Usuario directamente
            startActivity(new Intent(PantallaLogin.this, Usuario.class));
            finish();
        }

        // Configurar el botón para mostrar y ocultar la contraseña
        showPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para mostrar y ocultar la contraseña
                if (passwordEditText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Si la contraseña es visible, cambiar a contraseña oculta
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showPasswordButton.setImageResource(R.drawable.seeoff); // Cambiar icono a ojo cerrado
                } else {
                    // Si la contraseña está oculta, cambiar a contraseña visible
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showPasswordButton.setImageResource(R.drawable.see); // Cambiar icono a ojo abierto
                }
                // Mover el cursor al final del texto
                passwordEditText.setSelection(passwordEditText.length());
            }
        });
    }

    public void onLoginClicked(View view) {
        progressBar.setVisibility(View.VISIBLE);
        loginPost();
    }

    public void loginPost() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String postUrl = "https://api-production-c57e.up.railway.app/api/login";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JSONObject postData = new JSONObject();
        try {
            postData.put("nombre_usuario", username);
            postData.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, postUrl, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                try {
                    String token = response.getString("token");
                    sessionManager.saveAuthToken(token);

                    startActivity(new Intent(PantallaLogin.this, Usuario.class));
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                    errorTextView.setText("Error parsing token");
                    errorTextView.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                errorTextView.setVisibility(View.VISIBLE);
                usernameEditText.setText("");
                passwordEditText.setText("");
                usernameEditText.requestFocus();
                error.printStackTrace();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }
}
