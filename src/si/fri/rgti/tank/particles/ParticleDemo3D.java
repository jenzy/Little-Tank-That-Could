/*
 * Copyright (c) 2013, Oskar Veerhoek
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package si.fri.rgti.tank.particles;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_POINT_SIZE;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetFloat;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex3f;
import static org.lwjgl.util.glu.GLU.gluErrorString;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

/**
 * A 3D particle system. The system continually emits particles in a randomised direction from the emitter location.
 * Gravity affects particles. Particles gradually fade away, the longer they live. The left and right arrow keys pan the
 * scene. The mouse wheel zooms in or out. Pressing the left mouse button temporarily stops particle generation.
 */
public class ParticleDemo3D {

    private static ParticleEmitter particleEmitter = new ParticleEmitterBuilder()
            .setEnable3D(true)
            .setInitialVelocity(new Vector3f(0, 0, 0))
            .setGravity(new Vector3f(0, -0.00001f, 0))
            .setSpawningRate(50)
            .setParticleLifeTime(500)
            .setPointSize(5f)
            .setVelocityModifier(10f)
            .createParticleEmitter();
    private static float zoom = 1.0f;
    private static double step = 0;
    private static boolean rotateDirection = false;
    private static boolean rotate = false;

    public static void main(String[] args) {
        setUpDisplay();
        setUpMatrices();
        setUpStates();
        while (!Display.isCloseRequested()) {
            input();
            logic();
            render();
            refresh();
        }
        shutdown();
        System.exit(0);
    }


    private static void setUpMatrices() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(60, 640f / 480f, 0.3f, 100);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    private static void setUpDisplay() {
        try {
            Display.setDisplayMode(new DisplayMode(640, 480));
            Display.setTitle("Particle System");
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        }
    }

    private static void setUpStates() {
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.2f, 0.2f, 0.2f, 1);
    }

    private static void logic() {
        particleEmitter.update();
        if (rotate) {
            if (rotateDirection == /* left */ false) {
                step -= 0.03f;
            } else if (rotateDirection == /* right */ true) {
                step += 0.03f;
            }
        }
    }

    private static void input() {
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            rotateDirection = false;
            rotate = true;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            rotateDirection = true;
            rotate = true;
        } else {
            rotate = false;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
            particleEmitter.setVelocityModifier(particleEmitter.getVelocityModifier() * 1.01f);
        } else if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
            particleEmitter.setVelocityModifier(particleEmitter.getVelocityModifier() / 1.01f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
            particleEmitter.setGravity((Vector3f) particleEmitter.getGravity().scale(1.01f));
        } else if (Keyboard.isKeyDown(Keyboard.KEY_SEMICOLON)) {
            particleEmitter.setGravity((Vector3f) particleEmitter.getGravity().scale(0.99009900990099f));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
            particleEmitter.setSpawningRate(particleEmitter.getSpawningRate() * 1.01f);
        } else if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            particleEmitter.setSpawningRate(particleEmitter.getSpawningRate() / 1.01f);
        }
        float pointSize = glGetFloat(GL_POINT_SIZE);
        if (Keyboard.isKeyDown(Keyboard.KEY_T) && pointSize < 50) {
            glPointSize(pointSize * 1.01f);
        } else if (Keyboard.isKeyDown(Keyboard.KEY_G) && pointSize > 0) {
            glPointSize(pointSize / 1.01f);
        }
        float zoomModifier = -Mouse.getDWheel() / 12000f;
        if (zoomModifier < 0) {
            if (zoom + zoomModifier > 0.15f) {
                zoom += zoomModifier;
            }
        } else if (zoomModifier > 0) {
            zoom += zoomModifier;
        }
    }

    private static void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();
        GLU.gluLookAt((float) Math.sin(step) * 3 * zoom, 0, (float) Math.cos(step) * 3 * zoom, 0, 0, 0, 0, 1, 0);

        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex3f(-1.5f, -0.7f, -1.5f);
        glTexCoord2f(1, 0);
        glVertex3f(+1.5f, -0.7f, -1.5f);
        glTexCoord2f(1, 1);
        glVertex3f(+1.5f, -0.7f, +1.5f);
        glTexCoord2f(0, 1);
        glVertex3f(-1.5f, -0.7f, +1.5f);
        glEnd();
        glBindTexture(GL_TEXTURE_2D, 0);

        particleEmitter.draw();
    }

    private static void refresh() {
        Display.sync(60);
        Display.update();
    }

    private static void shutdown() {
        System.err.println(gluErrorString(glGetError()));
        Display.destroy();
    }
}