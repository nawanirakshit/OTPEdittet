# OTP EditText
 Displaying OTP View without any library and easy initialization
 
 ![alt text](https://github.com/nawanirakshit/OTPEdittet/blob/main/screenshot/otp_screenshot.png?raw=true)

## How to display  OTP Edit Text in a View

```
    //Change package name as per your package
    <com.rakshit.otpedittet.optview.OTPView
        android:id="@+id/otp_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:autofillHints=""
        android:importantForAutofill="no"
        android:inputType="number"
        app:otp_cursorColor="@color/red"
        app:otp_filledTextSize="24sp"
        app:otp_highlightedTextSize="24sp"
        app:otp_itemCount="4"
        app:otp_itemHeight="56sp"
        app:otp_itemWidth="48sp"
        app:otp_marginBetween="8dp"
        app:otp_showCursor="true"
        app:otp_textColor="@color/black"
        app:otp_textSize="24sp" />

```

## Character update listener
###### Can be used to enable/disable a button before entering all the reqired data in the view

```
  otpView.setOnCharacterUpdatedListener {
            if (it) {
                //Enable Submit button
                Log.i("MainActivity", "The view is filled")
            } else {
                //Disable Submit button
                Log.i("MainActivity", "The view is NOT Filled")
            }
        }
```

## Finished Listener
###### When all the item count is filled we can automatically proceed further or consume API as per our needs

```

 otpView.setOnFinishListener {
            //when all input fields are completely filled
            Log.i("MainActivity", it)
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
        
 ```
 
 #Easy Right?
 
