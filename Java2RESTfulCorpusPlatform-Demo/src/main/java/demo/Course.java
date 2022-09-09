package demo;

// Example class use by Demo main.Server and Demo main.Client
// Just a plain old Java object
class Course {
    String courseName;
    int SAcount;

    public Course(){
        this.courseName = "";
        this.SAcount = -1;
    }

    public Course(String courseName, int SAcount) {
        this.courseName = courseName;
        this.SAcount = SAcount;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setSAcount(int SAcount) {
        this.SAcount = SAcount;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getSAcount() {
        return SAcount;
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseName='" + courseName + '\'' +
                ", SAcount=" + SAcount +
                '}';
    }


}
