package com.example.tpdm_piedra_papel_tijera;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    FirebaseAuth fba;
    FirebaseAuth.AuthStateListener asl;
    DatabaseReference baseDeDatos;
    FirebaseUser usuario;
    boolean usr_local_tiro=false, finalizo_juego=false, dormir=true;
    String usr_local_tirada="nada";
    Button cerrar, reiniciar;
    ImageView imgJ1, imgJ2;
    TextView usuarioJ1, usuarioJ2, estadoJ1, estadoJ2, puntosJ1, puntosJ2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        cerrar=findViewById(R.id.cerrar);
        reiniciar=findViewById(R.id.reiniciar);
        imgJ1=findViewById(R.id.imgJ1);
        imgJ2=findViewById(R.id.imgJ2);
        usuarioJ1=findViewById(R.id.usuarioJ1);
        usuarioJ2=findViewById(R.id.usuarioJ2);
        estadoJ1=findViewById(R.id.estadoJ1);
        estadoJ2=findViewById(R.id.estadoJ2);
        puntosJ1=findViewById(R.id.puntosJ1);
        puntosJ2=findViewById(R.id.puntosJ2);

        baseDeDatos = FirebaseDatabase.getInstance().getReference();

        fba=FirebaseAuth.getInstance();
        asl=new FirebaseAuth.AuthStateListener(){
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                usuario=firebaseAuth.getCurrentUser();
                if (usuario==null){
                    Toast.makeText(MainActivity.this, "No esta logueado", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this,Main2Activity.class));
                }else{
                    Map<String, Object> nuevoUsuario = new HashMap<>();
                    nuevoUsuario.put("puntos", 0);
                    nuevoUsuario.put("tirada", "nada");
                    nuevoUsuario.put("tiro", false);
                    nuevoUsuario.put("estado", "Listo...");

                    baseDeDatos.child("sala").child(usuario.getEmail().split("@")[0]).setValue(nuevoUsuario);
                }
            }
        };

        baseDeDatos.child("sala").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()>2 || usuario==null)
                    return;
                if (dataSnapshot.getChildrenCount() == 1) {
                    puntosJ2.setText("0");
                    usuarioJ2.setText(" ");
                    estadoJ2.setText("Esperando jugador....");
                    imgJ2.setImageDrawable(getResources().getDrawable(R.drawable.esperando));
                }
                for (final DataSnapshot temporal : dataSnapshot.getChildren()) {
                    baseDeDatos.child("sala").child(temporal.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Usuario us = dataSnapshot.getValue(Usuario.class);
                            if (us != null) {
                                if(!usuario.getEmail().split("@")[0].equals(temporal.getKey())){
                                    puntosJ2.setText(us.getPuntos()+"");
                                    usuarioJ2.setText(temporal.getKey());
                                    estadoJ2.setText(us.getEstado());
                                    switch (us.getTirada()){
                                        case "piedra":
                                            imgJ2.setImageDrawable(getResources().getDrawable(R.drawable.piedra));
                                            break;
                                        case "papel":
                                            imgJ2.setImageDrawable(getResources().getDrawable(R.drawable.papel));
                                            break;
                                        case "tijera":
                                            imgJ2.setImageDrawable(getResources().getDrawable(R.drawable.tijera));
                                            break;
                                        case "nada":
                                            imgJ2.setImageDrawable(getResources().getDrawable(R.drawable.listo));
                                            break;
                                    }
                                    if(Integer.parseInt(puntosJ1.getText().toString())>=3&&!finalizo_juego){
                                        finalizo_juego=true;
                                        AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);
                                        alerta.setTitle("¡USUARIO: "+usuarioJ1.getText().toString()+" HA GANADO!")
                                                .setPositiveButton("Reiniciar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int ift) {
                                                        Map<String, Object> nuevoUsuario = new HashMap<>();
                                                        nuevoUsuario.put("puntos", 0);
                                                        nuevoUsuario.put("tirada", "nada");
                                                        nuevoUsuario.put("tiro", false);
                                                        nuevoUsuario.put("estado", "Listo...");

                                                        baseDeDatos.child("sala").child(temporal.getKey()).setValue(nuevoUsuario);
                                                        baseDeDatos.child("sala").child(usuario.getEmail().split("@")[0]).setValue(nuevoUsuario);
                                                        finalizo_juego=false;
                                                        dialogInterface.dismiss();
                                                    }
                                                }).setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                fba.signOut();
                                                finalizo_juego=false;
                                                dialog.dismiss();
                                            }
                                        });
                                        alerta.show();
                                    }

                                    if(Integer.parseInt(puntosJ2.getText().toString())>=3&&!finalizo_juego){
                                        finalizo_juego=true;
                                        AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);
                                        alerta.setTitle("¡USUARIO: "+usuarioJ2.getText().toString()+" HA GANADO!")
                                                .setPositiveButton("Reiniciar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int ift) {
                                                        Map<String, Object> nuevoUsuario = new HashMap<>();
                                                        nuevoUsuario.put("puntos", 0);
                                                        nuevoUsuario.put("tirada", "nada");
                                                        nuevoUsuario.put("tiro", false);
                                                        nuevoUsuario.put("estado", "Listo...");

                                                        baseDeDatos.child("sala").child(temporal.getKey()).setValue(nuevoUsuario);
                                                        baseDeDatos.child("sala").child(usuario.getEmail().split("@")[0]).setValue(nuevoUsuario);
                                                        finalizo_juego=false;
                                                        dialogInterface.dismiss();
                                                    }
                                                }).setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                fba.signOut();
                                                finalizo_juego=false;
                                                dialog.dismiss();
                                            }
                                        });
                                        alerta.show();
                                    }

                                    if(usr_local_tiro && us.isTiro()){
                                        if(obtenerGanador(usr_local_tirada, us.getTirada())==1){
                                            Map<String, Object> nuevoUsuario = new HashMap<>();
                                            nuevoUsuario.put("puntos", Integer.parseInt(puntosJ1.getText().toString())+1);
                                            nuevoUsuario.put("tirada", "nada");
                                            nuevoUsuario.put("tiro", false);
                                            nuevoUsuario.put("estado", "Listo...");

                                            baseDeDatos.child("sala").child(usuario.getEmail().split("@")[0]).setValue(nuevoUsuario);
                                            nuevoUsuario.put("puntos", us.getPuntos());
                                            baseDeDatos.child("sala").child(temporal.getKey()).setValue(nuevoUsuario);
                                        }else if(obtenerGanador(usr_local_tirada, us.getTirada())==2){
                                            Map<String, Object> nuevoUsuario = new HashMap<>();
                                            nuevoUsuario.put("puntos", us.getPuntos()+1);
                                            nuevoUsuario.put("tirada", "nada");
                                            nuevoUsuario.put("tiro", false);
                                            nuevoUsuario.put("estado", "Listo...");

                                            baseDeDatos.child("sala").child(temporal.getKey()).setValue(nuevoUsuario);
                                            nuevoUsuario.put("puntos", Integer.parseInt(puntosJ1.getText().toString()));
                                            baseDeDatos.child("sala").child(usuario.getEmail().split("@")[0]).setValue(nuevoUsuario);
                                        }else{
                                            Map<String, Object> nuevoUsuario = new HashMap<>();
                                            nuevoUsuario.put("puntos", us.getPuntos());
                                            nuevoUsuario.put("tirada", "nada");
                                            nuevoUsuario.put("tiro", false);
                                            nuevoUsuario.put("estado", "Listo...");

                                            baseDeDatos.child("sala").child(temporal.getKey()).setValue(nuevoUsuario);
                                            nuevoUsuario.put("puntos", Integer.parseInt(puntosJ1.getText().toString()));
                                            baseDeDatos.child("sala").child(usuario.getEmail().split("@")[0]).setValue(nuevoUsuario);
                                        }
                                        usr_local_tiro=false;
                                        usr_local_tirada="nada";
                                    }

                                }else{
                                    puntosJ1.setText(us.getPuntos()+"");
                                    usuarioJ1.setText(usuario.getEmail());
                                    estadoJ1.setText(us.getEstado());
                                    switch (us.getTirada()){
                                        case "piedra":
                                            imgJ1.setImageDrawable(getResources().getDrawable(R.drawable.piedra));
                                            break;
                                        case "papel":
                                            imgJ1.setImageDrawable(getResources().getDrawable(R.drawable.papel));
                                            break;
                                        case "tijera":
                                            imgJ1.setImageDrawable(getResources().getDrawable(R.drawable.tijera));
                                            break;
                                        case "nada":
                                            imgJ1.setImageDrawable(getResources().getDrawable(R.drawable.listo));
                                            break;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDeDatos.child("sala").child(usuario.getEmail().split("@")[0]).removeValue();
                fba.signOut();
            }
        });
        reiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDeDatos.child("sala").removeValue();
                fba.signOut();
            }
        });
    }

    protected void onStart(){
        super.onStart();
        fba.addAuthStateListener(asl);

    }
    protected void onStop(){
        super.onStop();
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(this);
        fba.removeAuthStateListener(asl);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(usr_local_tiro)
            return;
        if(event.values[0]>20){
            Random r = new Random();
            int valor = r.nextInt(3);
            Map<String, Object> nuevoUsuario = new HashMap<>();
            nuevoUsuario.put("puntos", Integer.parseInt(puntosJ1.getText().toString()));
            nuevoUsuario.put("tiro", true);
            nuevoUsuario.put("estado", "Esperando tirada del otro jugador..");
            switch (valor){
                case 0:
                    nuevoUsuario.put("tirada", "piedra");
                    usr_local_tirada="piedra";
                    break;
                case 1:
                    nuevoUsuario.put("tirada", "papel");
                    usr_local_tirada="papel";
                    break;
                case 2:
                    nuevoUsuario.put("tirada", "tijera");
                    usr_local_tirada="tijera";
                    break;
                default:
                    nuevoUsuario.put("tirada", "nada");
                    usr_local_tirada="nada";
                    break;
            }
            baseDeDatos.child("sala").child(usuario.getEmail().split("@")[0]).setValue(nuevoUsuario);
            usr_local_tiro=true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            sm.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
        }
    }

    int obtenerGanador(String tirada_jug_1, String tirada_jug_2){
        if(tirada_jug_1.equals("piedra")&&tirada_jug_2.equals("piedra"))
            return 0;
        if(tirada_jug_1.equals("piedra")&&tirada_jug_2.equals("papel"))
            return 2;
        if(tirada_jug_1.equals("piedra")&&tirada_jug_2.equals("tijera"))
            return 1;
        if(tirada_jug_1.equals("papel")&&tirada_jug_2.equals("piedra"))
            return 1;
        if(tirada_jug_1.equals("papel")&&tirada_jug_2.equals("papel"))
            return 0;
        if(tirada_jug_1.equals("papel")&&tirada_jug_2.equals("tijera"))
            return 2;
        if(tirada_jug_1.equals("tijera")&&tirada_jug_2.equals("piedra"))
            return 2;
        if(tirada_jug_1.equals("tijera")&&tirada_jug_2.equals("papel"))
            return 1;
        if(tirada_jug_1.equals("tijera")&&tirada_jug_2.equals("tijera"))
            return 0;
        return 22;
    }

}
