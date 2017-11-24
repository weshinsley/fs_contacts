package com.teapotrecords.fscontacts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
  static String version = "1.0";
  static String author = "wes.hinsley@gmail.com";
  static String copyright = "(C) 2017 Teapot Records";

  Toast currentToast = null;

  public static boolean validEmail(String e) {
    return ((e != null) &&
           android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches());
  }

  public void bigToast(Context context, String t, int ts) {
    SpannableStringBuilder bt = new SpannableStringBuilder(t);
    bt.setSpan(new RelativeSizeSpan(1.35f), 0, t.length(), 0);
    if (currentToast!=null) {
      currentToast.cancel();
    }
    currentToast = Toast.makeText(context, bt, ts);
    currentToast.show();
  }

  public void saveEntry(Context c, String n, String e, String f) {
    File file = new File(c.getFilesDir(), f);
    PrintWriter PW;
    try {
      if (!file.exists()) PW = new PrintWriter(new FileOutputStream(file));
      else PW = new PrintWriter(new FileOutputStream(file, true));
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MMM-dd", Locale.UK);
      String d = df.format(new Date());
      PW.println(d+"\t"+n+"\t"+e+"\t");
      PW.close();
    } catch (Exception ex) {
      bigToast(c,ex.getMessage(),Toast.LENGTH_LONG);
    }
  }
  public void saveEntry(Context c, String n, String e, boolean fs, boolean lhop) {
    if (fs) saveEntry(c, n, e, "fs.txt");
    if (lhop) saveEntry(c, n, e, "lhop.txt");
  }

  public void addFile(Context c, StringBuilder sb, String f, String t) {
    File file = new File(c.getFilesDir(), f);
    String contents = null;
    if (file.exists()) {
      try {
        contents = new Scanner(file).useDelimiter("\\Z").next();
      } catch (Exception ex) {
        bigToast(c,ex.getMessage(), Toast.LENGTH_LONG);
      }
    }
    if ((contents!=null) && (contents.length()>3)) {
      sb.append(t).append(contents).append("\n\n");
    }
  }

  public void clearFiles(Context c) {
    File file = new File(c.getFilesDir(), "fs.txt");
    file.delete();
    file = new File(c.getFilesDir(), "lhop.txt");
    file.delete();
  }

  public void sendEmail(Context c, String theEmail) {
    StringBuilder theText = new StringBuilder("Below are the updates to the mailing lists.\n\n");
    addFile(c, theText, "fs.txt", "FILLING STATION\n---------------\n\n");
    addFile(c, theText, "lhop.txt", "LOCAL HOUSES OF PRAYER\n----------------------\n\n");
    theText.append("End of message!\n");
    Intent i = new Intent(Intent.ACTION_SEND);
    i.setType("message/rfc822");
    i.putExtra(Intent.EXTRA_EMAIL, new String[]{theEmail});
    i.putExtra(Intent.EXTRA_SUBJECT, "FS/LHOP Mailing List Updates");
    i.putExtra(Intent.EXTRA_TEXT, theText.toString());
    try {
      startActivity(Intent.createChooser(i, "Sending mail..."));
    } catch (Exception e) {
      bigToast(c,"Mail not setup on this android", Toast.LENGTH_LONG);
    }

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    final Context c = MainActivity.this;
    AlertDialog.Builder builder;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      builder = new AlertDialog.Builder(c, android.R.style.Theme_Material_Dialog_Alert);
    } else {
      builder = new AlertDialog.Builder(c);
    }

    switch (item.getItemId()) {
      case R.id.about:
        builder.setTitle("Filling Station Contact Collector");
        builder.setMessage("Version: "+version+"\nAuthor: "+author+"\n"+copyright);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });
        builder.show();
        return true;

      case R.id.send_mail:
        final SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        String admin_mail = prefs.getString("admin_mail", "");
        builder.setTitle("Admin Mail Address:");
        final EditText input = new EditText(this);
        input.setText(admin_mail);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setTextColor(Color.WHITE);
        input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f);
        builder.setView(input);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            String email = input.getText().toString();
            if (validEmail(email)) {
              prefs.edit().putString("admin_mail", email).apply();
              sendEmail(c,email);
              bigToast(c, "Mail Passed to Mail Client",Toast.LENGTH_LONG);
            } else {
              bigToast(c, "Invalid Email", Toast.LENGTH_LONG);
            }
          }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });
        builder.show();
        return true;

      case R.id.clear_all:
        builder.setTitle("Clear Stored Details");
        builder.setMessage("Really delete stored mails?");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            clearFiles(c);
            bigToast(c, "Stored addresses cleared",Toast.LENGTH_LONG);
          }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });
        builder.show();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final Button signUp = findViewById(R.id.addButton);
    signUp.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        final CheckBox fsSignup = findViewById(R.id.fsSignup);
        final CheckBox lhopSignup = findViewById(R.id.lhopSignup);
        final EditText name = findViewById(R.id.nameText);
        String theName = name.getText().toString().trim();
        final EditText email = findViewById(R.id.emailText);
        String eMail = email.getText().toString().trim();
        final Context context = getApplicationContext();

        if ((!fsSignup.isChecked()) && (!lhopSignup.isChecked())) {
          bigToast(context, getString(R.string.no_list_clicked), Toast.LENGTH_LONG);
        } else if (theName.length() == 0) {
          bigToast(context, getString(R.string.no_name), Toast.LENGTH_LONG);
        } else if ((eMail.length() == 0) || (!validEmail(eMail))) {
          bigToast(context, getString(R.string.bad_email), Toast.LENGTH_LONG);
        } else {
          saveEntry(context, theName,eMail,fsSignup.isChecked(), lhopSignup.isChecked());
          name.setText("");
          email.setText("");
          fsSignup.setChecked(false);
          lhopSignup.setChecked(false);
          bigToast(context, getString(R.string.thank_you_text), Toast.LENGTH_LONG);
        }
      }
    });
  }
}
