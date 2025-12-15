package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConfettiView extends View {

    private final List<Particle> particles = new ArrayList<>();
    private long lastTime = 0;
    private boolean isAnimating = false;
    private final Random random = new Random();

    public ConfettiView(Context context) {
        super(context);
    }

    public ConfettiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConfettiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 启动爆炸效果
     *
     * @param drawables 图形资源列表
     * @param colors    颜色列表
     * @param x         发射点X坐标 (相对于View)
     * @param y         发射点Y坐标 (相对于View)
     */
    public void explode(List<Drawable> drawables, List<Integer> colors, float x, float y) {
        particles.clear();

        // 模拟 Konfetti 的发射数量，这里生成约 60 个粒子
        int count = 60;

        // 这里控制爆炸力度的范围
        float minForce = 2f;  // 最小爆炸力度
        float maxForce = 15f; // 最大爆炸力度

        for (int i = 0; i < count; i++) {
            Drawable originalDrawable = drawables.get(random.nextInt(drawables.size()));
            int color = colors.get(random.nextInt(colors.size()));

            // 复制 Drawable 以便独立修改颜色和边界
            Drawable d = originalDrawable.getConstantState().newDrawable().mutate();
            d.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            // 随机大小 (模拟 12dp - 30dp)
            float density = getResources().getDisplayMetrics().density;
            float sizeDp = 12 + random.nextFloat() * (30 - 12);
            int sizePx = (int) (sizeDp * density);

            // 初始速度和角度
            // 主要向上爆炸 (角度范围 150° - 390°)
            double angle = Math.toRadians(150 + random.nextDouble() * 240);

            // 计算随机速度
            float rawSpeed = minForce + random.nextFloat() * (maxForce - minForce);

            // 将速度转换为像素单位
            float speed = rawSpeed * density * 0.6f;

            float vx = (float) (Math.cos(angle) * speed);
            float vy = (float) (Math.sin(angle) * speed);

            particles.add(new Particle(d, x, y, vx, vy, sizePx));
        }

        lastTime = System.currentTimeMillis();
        isAnimating = true;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isAnimating || particles.isEmpty()) return;

        long now = System.currentTimeMillis();
        // 限制最大帧间隔，防止后台切回时跳跃过大
        long deltaTime = (lastTime == 0) ? 0 : Math.min(now - lastTime, 50);
        lastTime = now;

        boolean allDead = true;

        for (Particle p : particles) {
            if (p.isAlive()) {
                p.update(deltaTime);
                p.draw(canvas);
                allDead = false;
            }
        }

        if (allDead) {
            isAnimating = false;
            particles.clear();
        } else {
            invalidate();
        }
    }

    public void reset() {
        particles.clear();
        isAnimating = false;
        invalidate();
    }

    private static class Particle {
        float x, y;
        float vx, vy;
        // 重力效果：稍微改小一点(原0.2f)，让它飘得久一点，方便看清渐隐
        float gravity = 0.15f;
        float alpha = 1.0f;
        long totalLifeTime = 2500; // 稍微延长总存活时间到 2.5s
        long currentLife = 0;
        Drawable drawable;
        int size;
        float damping = 0.96f; // 阻尼

        Particle(Drawable drawable, float x, float y, float vx, float vy, int size) {
            this.drawable = drawable;
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
        }

        void update(long dt) {
            currentLife += dt;

            // 应用物理
            x += vx * (dt / 10f);
            y += vy * (dt / 10f);

            // 应用阻尼
            vx *= damping;

            // 应用重力
            vy += gravity * (dt / 5f);

            // === 渐隐效果优化 ===
            // 从生命周期的 20% 处就开始渐隐（原代码是 70%，导致还没来得及消失就掉出屏幕了）
            // 这样能确保你看到明显的慢慢消失效果
            float fadeThreshold = 0.2f;

            if (currentLife > totalLifeTime * fadeThreshold) {
                float remaining = totalLifeTime - currentLife;
                if (remaining < 0) remaining = 0;

                // 计算剩余寿命比例
                float fadeDuration = totalLifeTime * (1.0f - fadeThreshold);
                alpha = remaining / fadeDuration;
            }
        }

        boolean isAlive() {
            return currentLife < totalLifeTime;
        }

        void draw(Canvas canvas) {
            if (alpha <= 0) return;

            // 关键：必须设置 Drawable 的 Alpha
            drawable.setAlpha((int) (alpha * 255));

            int halfSize = size / 2;
            int left = (int) x - halfSize;
            int top = (int) y - halfSize;
            int right = (int) x + halfSize;
            int bottom = (int) y + halfSize;

            drawable.setBounds(left, top, right, bottom);
            drawable.draw(canvas);
        }
    }
}
