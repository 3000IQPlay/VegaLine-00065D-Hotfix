package ru.govno.client.utils.Render;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL20;
import ru.govno.client.utils.Render.RenderUtils;

public class ShaderUtil2 {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private int programID;

    public void setUniformf(String name, float ... args) {
        try {
            int loc = GL20.glGetUniformLocation((int)this.programID, (CharSequence)name);
            switch (args.length) {
                case 1: {
                    GL20.glUniform1f((int)loc, (float)args[0]);
                    break;
                }
                case 2: {
                    GL20.glUniform2f((int)loc, (float)args[0], (float)args[1]);
                    break;
                }
                case 3: {
                    GL20.glUniform3f((int)loc, (float)args[0], (float)args[1], (float)args[2]);
                    break;
                }
                case 4: {
                    GL20.glUniform4f((int)loc, (float)args[0], (float)args[1], (float)args[2], (float)args[3]);
                }
            }
        }
        catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public void setUniformColor(String name, int color) {
        this.setUniformf(name, (float)RenderUtils.red(color) / 255.0f, (float)RenderUtils.green(color) / 255.0f, (float)RenderUtils.blue(color) / 255.0f);
    }

    public void init() {
        if (this.programID != 0) {
            GL20.glUseProgram((int)this.programID);
        }
    }

    public void unload() {
        GL20.glUseProgram((int)0);
    }

    public void setUniformi(String name, int ... args) {
        int loc = GL20.glGetUniformLocation((int)this.programID, (CharSequence)name);
        if (args.length > 1) {
            GL20.glUniform2i((int)loc, (int)args[0], (int)args[1]);
        } else {
            GL20.glUniform1i((int)loc, (int)args[0]);
        }
    }

    public ShaderUtil2(String fragmentShaderLoc, String vertexShaderLoc) {
        try {
            int program = GL20.glCreateProgram();
            try {
                int fragmentShaderID = this.createShader(mc.getResourceManager().getResource(new ResourceLocation(fragmentShaderLoc)).getInputStream(), 35632);
                GL20.glAttachShader((int)program, (int)fragmentShaderID);
                int vertexShaderID = this.createShader(mc.getResourceManager().getResource(new ResourceLocation(vertexShaderLoc)).getInputStream(), 35633);
                GL20.glAttachShader((int)program, (int)vertexShaderID);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            GL20.glLinkProgram((int)program);
            if (GL20.glGetProgrami((int)program, (int)35714) == 0) {
                throw new IllegalStateException("Shader failed to link!");
            }
            this.programID = program;
        }
        catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public int getUniform(String name) {
        return GL20.glGetUniformLocation((int)this.programID, (CharSequence)name);
    }

    public ShaderUtil2(String fragmentShaderLoc) {
        this(fragmentShaderLoc, "vegaline/modules/esp/shaders/vertex.vsh");
    }

    public void attach() {
        try {
            GL20.glUseProgram((int)this.programID);
        }
        catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public void detach() {
        GL20.glUseProgram((int)0);
    }

    public static String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String line;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private int createShader(InputStream inputStream, int shaderType) {
        try {
            int shader = GL20.glCreateShader((int)shaderType);
            GL20.glShaderSource((int)shader, (CharSequence)ShaderUtil2.readInputStream(inputStream));
            GL20.glCompileShader((int)shader);
            return shader;
        }
        catch (Exception e) {
            e.fillInStackTrace();
            return 0;
        }
    }
}

