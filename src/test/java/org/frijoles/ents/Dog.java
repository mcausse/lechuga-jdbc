package org.frijoles.ents;

public class Dog {

    IdDog id;
    int age;
    ESex sex;
    boolean dead;

    public Dog() {
        super();
    }

    public Dog(IdDog id, int age, ESex sex, boolean dead) {
        super();
        this.id = id;
        this.age = age;
        this.sex = sex;
        this.dead = dead;
    }

    public IdDog getId() {
        return id;
    }

    public void setId(IdDog id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public ESex getSex() {
        return sex;
    }

    public void setSex(ESex sex) {
        this.sex = sex;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    @Override
    public String toString() {
        return "Dog [id=" + id + ", age=" + age + ", sex=" + sex + ", dead=" + dead + "]";
    }

}