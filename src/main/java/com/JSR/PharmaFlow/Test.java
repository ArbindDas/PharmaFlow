package com.JSR.PharmaFlow;

import lombok.Data;

import java.awt.*;

@Data
class Marks{

    private Double Math;
    private Double Science;
    private Double Nepali;
    private Double English;
    private Double computer;
}
@Data
class Student{

    private String name;
    private Integer roll;
}

@Data
class CalculateStudentMarks{


    public void TotalMarks(Student student  , Marks marks){

        student.setName("Arbind Das");
        student.setRoll(1);

        marks.setScience(90.0);
        marks.setComputer(89.0);
        marks.setNepali(97.0);
        marks.setEnglish(88.0);
        marks.setMath(99.0);


        System.out.println("student name is  : " +  student.getName());
        System.out.println("student roll is :" +student.getRoll() );


        System.out.println("the student given marks");
        System.out.println(marks.getComputer());
        System.out.println( marks.getEnglish());


        System.out.println(  marks.getScience());
        System.out.println(  marks.getNepali());


        System.out.println( marks.getMath());


        double totalMarks  = marks.getComputer() + marks.getNepali() + marks.getScience()+ marks.getMath()+marks.getEnglish();

        System.out.println("total marks is : "+ totalMarks);


        double percentage = totalMarks/5;

        System.out.println("percentage is : "+percentage);


        if (percentage>=90 && percentage<=100){
            System.out.println("Grade : A+");
        }
        


    }


}

// Your custom Frame class
class MM extends Frame {
    String L1;
    String L2;
    TextField t1;
    TextField t2;
    Button b1;

    public MM() {
        // Set title
        super("My Frame");

        // Initialize components
        Label label1 = new Label("Roll:");
        Label label2 = new Label("Name:");

        t1 = new TextField(20);  // width 20
        t2 = new TextField(20);

        b1 = new Button("Submit");

        // Set layout
        setLayout(new FlowLayout());

        // Add components
        add(label1);
        add(t1);
        add(label2);
        add(t2);
        add(b1);

        // Frame settings
        setSize(300, 200);
        setVisible(true);
    }

    public void setL1(String l1) {
        this.L1 = l1;
    }

    public void setL2(String l2) {
        this.L2 = l2;
    }

    public void setT1(TextField t1) {
        this.t1 = t1;
    }

    public void setT2(TextField t2) {
        this.t2 = t2;
    }

    public void setB1(Button b1) {
        this.b1 = b1;
    }
}

// Test class
public class Test {
    public static void main(String[] args) {
        new MM();  // just create the frame
    }
}

