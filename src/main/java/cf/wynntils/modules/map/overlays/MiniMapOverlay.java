package cf.wynntils.modules.map.overlays;

import cf.wynntils.Reference;
import cf.wynntils.core.framework.overlays.Overlay;
import cf.wynntils.core.framework.rendering.textures.Textures;
import cf.wynntils.modules.map.MapModule;
import cf.wynntils.modules.map.configs.MapConfig;
import cf.wynntils.modules.map.instances.MapProfile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

public class MiniMapOverlay extends Overlay {

    public MiniMapOverlay() {
        super("Mini Map", 100, 100, true, 0, 0, 10, 10, OverlayGrowFrom.TOP_LEFT);
    }

    private static int mapSize = 100;
    private static int zoom = 100;

    @Override
    public void render(RenderGameOverlayEvent.Pre e) {
        if(!Reference.onWorld || e.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if(!MapModule.getModule().getMainMap().isReadyToUse()) return;

        MapProfile map = MapModule.getModule().getMainMap();

        //calculates the extra size to avoid rotation overpass
        int extraSize = 0;
        if(MapConfig.INSTANCE.followPlayerRotation && MapConfig.INSTANCE.mapFormat == MapConfig.MapFormat.SQUARE) extraSize = 100;

        zoom = 100 + extraSize;

        float minX = (float)(mc.player.posX-(map.getCenterX() - extraSize/4)-(mapSize + extraSize)/2 + zoom);  float maxX = minX+(mapSize + extraSize)/2 - 2 * zoom;
        float minZ = (float)(mc.player.posZ-(map.getCenterZ() - extraSize/4)-(mapSize + extraSize)/2 + zoom);  float maxZ = minZ+(mapSize + extraSize)/2 - 2 * zoom;

        minX /= (float)map.getImageWidth(); maxX /= (float)map.getImageWidth();
        minZ /= (float)map.getImageHeight(); maxZ /= (float)map.getImageHeight();

        try{
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();

            //textures & masks
            if(MapConfig.INSTANCE.mapFormat == MapConfig.MapFormat.CIRCLE) {
                //drawRect(Textures.Map.circle_map, -5, -5, mapSize+5, mapSize+5, 0, 0, 110, 110);
                createMask(Textures.Masks.circle, 0, 0, mapSize, mapSize);
            }else{
                drawRect(Textures.Map.square_map, -6, -6, mapSize+6, mapSize+6, 0, 0, 112, 112);
                createMask(Textures.Masks.full, 0, 0, mapSize, mapSize);
            }

            //map texture
            map.bindTexture();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

            //rotation axis
            transformationOrigin(mapSize/2, mapSize/2);
            if(MapConfig.INSTANCE.followPlayerRotation)
                rotate(-MathHelper.fastFloor(mc.player.rotationYaw));

            //map quad
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.glBegin(GL11.GL_QUADS);
            {
                GlStateManager.glTexCoord2f(maxX,maxZ);
                GlStateManager.glVertex3f(position.getDrawingX() + mapSize + extraSize/2, position.getDrawingY() + mapSize + extraSize/2, 0);
                GlStateManager.glTexCoord2f(maxX,minZ);
                GlStateManager.glVertex3f(position.getDrawingX() + mapSize + extraSize/2, position.getDrawingY() - extraSize/2, 0);
                GlStateManager.glTexCoord2f(minX,minZ);
                GlStateManager.glVertex3f(position.getDrawingX() - extraSize/2,position.getDrawingY() - extraSize/2, 0);
                GlStateManager.glTexCoord2f(minX,maxZ);
                GlStateManager.glVertex3f(position.getDrawingX() - extraSize/2 ,position.getDrawingY() + mapSize + extraSize/2, 0);
            }
            GlStateManager.glEnd();

            //cursor & cursor rotation
            rotate(MathHelper.fastFloor(mc.player.rotationYaw));
            drawRectF(Textures.Map.pointer, mapSize/2 - 2.5f, mapSize/2 - 2.5f, mapSize/2 + 2.5f, mapSize/2 + 2.5f, 0f, 0f, 5f, 5f);

        }catch (Exception ex) { ex.printStackTrace(); }
    }

}