/*
 * Copyright (c) 2010, Oracle.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  * Neither the name of Oracle nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package clienteditor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Information about one clinet.
 * 
 * @author Jiri Vagner, Jan Stola
 */
public class Client {
    /** First name of the client. */
    private String firstName;
    /** Surname of the client. */
    private String surname;
    /** Nickname of the client. */
    private String nickname;
    /** Age of the client. */
    private int age;
    /** Sex of the client (0 - female, 1 - male). */
    private int sex;
    /** Marital status of the client (0 - single, 1 - married, 2 - separated, 3 - divorced) */
    private int maritalStatus;
    /** E-mail of the client. */
    private String email;
    /** Home web page of the client. */
    private String web;
    /** Instant messenger of the client. */
    private String im;
    
    // <editor-fold defaultstate="collapsed" desc="PropertyChange Stuff">
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Get Methods">
    public String getFirstName() {
        return firstName;
    }
    
    public String getSurname() {
        return surname;
    }

    public String getNickname() {
        return nickname;
    }
    
    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public String getWeb() {
        return web;
    }

    public String getIm() {
        return im;
    }

    public int getSex() {
        return sex;
    }
    
    public int getMaritalStatus() {
        return maritalStatus;
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Set Methods">
    public void setFirstName(String firstName) {
        String oldFirstName = this.firstName;
        this.firstName = firstName;
        changeSupport.firePropertyChange("firstName", oldFirstName, firstName);
    }
    
    public void setSurname(String surname) {
        String oldSurname = this.surname;
        this.surname = surname;
        changeSupport.firePropertyChange("surname", oldSurname, surname);
    }

    public void setNickname(String nickname) {
        String oldNickname = this.nickname;
        this.nickname = nickname;
        changeSupport.firePropertyChange("nickname", oldNickname, nickname);
    }

    public void setAge(int age) {
        int oldAge = this.age;
        this.age = age;
        changeSupport.firePropertyChange("age", oldAge, age);
    }

    public void setEmail(String email) {
        String oldEmail = this.email;
        this.email = email;
        changeSupport.firePropertyChange("email", oldEmail, email);
    }

    public void setWeb(String web) {
        String oldWeb = this.web;
        this.web = web;
        changeSupport.firePropertyChange("web", oldWeb, web);
    }

    public void setIm(String im) {
        String oldIm = this.im;
        this.im = im;
        changeSupport.firePropertyChange("im", oldIm, im);
    }

    public void setSex(int sex) {
        int oldSex = this.sex;
        this.sex = sex;
        changeSupport.firePropertyChange("sex", oldSex, sex);
    }
    
    public void setMaritalStatus(int maritalStatus) {
        int oldMaritalStatus = this.maritalStatus;
        this.maritalStatus = maritalStatus;
        changeSupport.firePropertyChange("maritalStatus", oldMaritalStatus, maritalStatus);
    }

    // </editor-fold>
    
    public static Client createTestClient() {
        Client client = new Client();
        client.setFirstName("George");
        client.setSurname("Foo");
        client.setNickname("Juraj");
        client.setAge(30);
        
        client.setEmail("g.foo@foo.org");
        client.setWeb("https://beansbinding.dev.java.net");
        client.setIm("ICQ: 53 25 89 76");
        
        client.setSex(1);
        client.setMaritalStatus(2);

        return client;
    }
}
