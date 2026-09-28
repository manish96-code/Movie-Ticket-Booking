import javax.swing.*;
import java.awt.*;

public class LoginPage extends JFrame{

    LoginPage(){
        setTitle("LoginPage");
        setSize(400,420);
        setLayout(null);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // title 
        JLabel titleLabel=new JLabel("LOGIN");
        titleLabel.setFont(new Font("Arial",Font.BOLD,28));
        titleLabel.setBounds(145,30,120,40);
        add(titleLabel);

        // username lable 
        JLabel userName = new JLabel("User Name");
        userName.setFont(new Font("Arial", Font.BOLD, 16));
        userName.setBounds(50,100,100,30);
        add(userName);

        // userName field
        JTextField UserNameField=new JTextField();
        UserNameField.setBounds(150,100,180,30);
        UserNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        add(UserNameField);

        // Password lable
        JLabel password =new JLabel("Password");
        password.setFont(new Font("Arial", Font.BOLD, 16));
        password.setBounds(50,150,100,30);
        add(password);

        // password text field
        JTextField passwordField = new JTextField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBounds(150,150,180,30);
        add(passwordField);

        // Login button
        JButton loginButton=new JButton("Login");
        loginButton.setBounds(90,220,100,35);
        add(loginButton);

        // clear button
        JButton clearButton=new JButton("clear");
        clearButton.setBounds(210,220,100,35);
        add(clearButton);

        // forgetButton
        JButton forgeButton=new JButton("Forget Password");
        forgeButton.setBounds(90,290,210,35);
        add(forgeButton);



        setVisible(true);

    }
    public static void main(String[] args) {
        new LoginPage();
    }
}

