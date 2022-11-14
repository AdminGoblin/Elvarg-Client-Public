package com.runescape.loginscreen.cinematic;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.runescape.Client;
import com.runescape.cache.Resource;
import com.runescape.cache.graphics.sprite.Sprite;
import com.runescape.draw.Rasterizer2D;
import com.runescape.draw.Rasterizer3D;
import com.runescape.loginscreen.cinematic.camera.Camera;
import com.runescape.loginscreen.cinematic.camera.CameraFrameOLD;
import com.runescape.loginscreen.cinematic.camera.CameraMove;
import com.runescape.loginscreen.cinematic.scenes.SceneTemplate;
import com.runescape.scene.MapRegion;
import com.runescape.util.Vector3;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GameState;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CinematicScene {
    private final Client client;


    List<SceneTemplate> llist = new LinkedList<SceneTemplate>();

    public CinematicScene(Client client) {
        this.client = client;

        blackWindow = new Sprite(Client.canvasWidth, Client.canvasHeight);
        Arrays.fill(blackWindow.myPixels, (255 << 24 ) + 0x050505);
        this.fadeToBlack.addListener((observable, oldVal, newVal) -> {
            if(newVal.doubleValue() >= 100) {
                setNextScene();
                resetMapData();
                this.prepareLoginScene();
            }
            if(newVal.doubleValue() <= 0) {
                resetSceneGraph();
                setNextCamera();
                this.fadeToBlack.set(-1);
            }
        });
        this.setupCamera();
        this.setupWorldMap();
        //randomizeMaps();

        setNextScene();
        setNextCamera();
        resetMapData();
        resetSceneGraph();
    }


    public void randomizeMaps() {
        long seed = System.nanoTime();
        List<Vector3> positions = Lists.newArrayList(mapPositions);
        List<CameraMove> cameras = Lists.newArrayList(mapCameraMoves);
        Collections.shuffle(positions, new Random(seed));
        Collections.shuffle(cameras, new Random(seed));

        mapPositions.clear();
        mapPositions.addAll(positions);

        mapCameraMoves.clear();
        mapCameraMoves.addAll(cameras);
    }
    private void setupWorldMap() {

        Vector3[] vecs = {
                Vector3.of(3223,3217, 0),
                //Vector3.of(3081,3251, 0)
        };
        mapPositions.addAll(Arrays.asList(vecs));
    }

    private void setupCamera() {

        for(int i = 0;i<1;i++) {
            CameraMove otherCameraMove = new CameraMove();

            otherCameraMove

                    .startInformation(Vector3.of(6711, 5920, -1901), 2032, 383)
                    .add(new CameraFrameOLD(Vector3.of(6597, 7213, -665), 2034, 128, 250))
                    .add(new CameraFrameOLD(Vector3.of(6454, 8791, -715), 1983, 133, 240))
                    .add(new CameraFrameOLD(Vector3.of(6452, 9108, -1017), 1951, 208, 245))
                    .add(new CameraFrameOLD(Vector3.of(6131, 10220, -1420), 1794, 288, 200))
                    .add(new CameraFrameOLD(Vector3.of(6135, 11541, -1420), 1277, 288, 175,true))
                    .add(new CameraFrameOLD(Vector3.of(7432, 11543, -1420), 772, 288, 175))
                    .add(new CameraFrameOLD(Vector3.of(7440, 10224, -1420), 256, 288, 175));


            this.mapCameraMoves.add(otherCameraMove);
        }

    }

    public void prepareLoginScene() {
        client.setGameState2(GameState.LOADING);
        System.out.println("LOADING");
        if(client.resourceProvider == null) {
            System.out.println("ODM NULL");
            return;
        }
        if(!loadedScenegraph) {
            try {
                if(this.regions.isEmpty()) {

                    startResourceProvider();
                    for(int x = 0;x<3;x++) {
                        for(int y = 0;y<3;y++) {
                            int newX = (worldX + (64 * x)) / 64;
                            int newY = (worldY + (64 * y)) / 64;
                            MapRegionData region = MapRegionData.builder()
                                    .regionX(newX)
                                    .regionY(newY)
                                    .landscape(client.resourceProvider.resolve(0, newY, newX))
                                    .objects(client.resourceProvider.resolve(1, newY, newX))
                                    .build();
                            if(region.getLandscape() != -1 && region.getObjects() != -1) {
                                System.out.println("Requesting");
                                region.requestFiles(client.resourceProvider);
                                regions.add(region);
                            }
                        }

                    }
                } else if(allMapsProvided()) {
                    loadedScenegraph = loadBackgroundMap();
                    setNextScene();
                    System.out.println("LOADED");
                    client.setGameState2(GameState.LOGIN_SCREEN_ANIMATED);
                }

            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    boolean loadedScenegraph = false;

    public boolean loadBackgroundMap() {
        boolean modelsPreloaded = regions.stream().allMatch(region -> MapRegion.method189(0, region.getObjectsData(), 0));
        if(!modelsPreloaded) {
            return false;
        }

        Client.setBounds();

        client.currentMapRegion = new MapRegion(client.tileFlags, client.tileHeights);

        client.getScene().method275(0);
        int baseX = (this.worldX / 64);
        int baseY = (this.worldY / 64);
        this.regions.forEach(region -> {
            int offsetX = (region.getRegionX() - baseX) * 64;
            int offsetY = (region.getRegionY() - baseY) * 64;
            client.currentMapRegion.method180(region. getLandscapeData(), offsetY, offsetX, region.getRegionX() * 64, region.getRegionY() * 64, client.collisionMaps);
            client.currentMapRegion.method190(offsetX, client.collisionMaps, offsetY, client.getScene(), region.getObjectsData());
        });

        client.currentMapRegion.createRegionScene(client.collisionMaps, client.getScene());
        return true;
    }



    public void render() {

        if (client.getGameState2() == GameState.LOADING) {
            client.drawLoadingMessage("Loading - please wait.");
        }

        if(loadedScenegraph) {

            Rasterizer3D.useViewport();

            Rasterizer2D.clear();
            if (Rasterizer3D.fieldOfView != Client.cameraZoom) {
                Rasterizer3D.fieldOfView = Client.cameraZoom;
            }

            client.getScene().minLevel = 0;

            try {
                cameraMove.apply(this);
                Camera camera = getCamera();
                client.showPrioritizedNPCs();
                client.showOtherNpcs();
                client.getScene().render(camera.getPosition().getX(), camera.getPosition().getY(), camera.getRotation(), camera.getPosition().getZ(), worldZ, camera.getTilt());
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            if(fadeFromBlack.get() > 0.0) {
                fadeFromBlack.set( fadeFromBlack.get() - (0.5));
                blackWindow.drawAdvancedSprite(0, 0, (int) Math.ceil(fadeFromBlack.get()));
            } else if(fadeToBlack.get() > 0) {
                fadeToBlack.set(fadeToBlack.get() - 0.5);
                blackWindow.drawAdvancedSprite(0, 0, 100 - (int) Math.ceil(fadeToBlack.get()));
            }
        } else {
            blackWindow.drawAdvancedSprite(0, 0, (int) Math.ceil(fadeFromBlack.get()));
            prepareLoginScene();

            //Npc npc = new Npc();
            //npc.x = 3224;
            //npc.y = 3219;
            //npc.height = 0;
           // npc.desc = NpcDefinition.lookup(239);
            ///Client.instance.npcs[0] = npc;
            //client.npcCount++;

        }

    }



    private void setNextScene() {

        if(worldX != 0 && worldY != 0)
            mapPositions.offer(Vector3.of(worldX, worldY, 0));

        Vector3 nextMap = mapPositions.poll();
        if(nextMap != null) {
            worldX = nextMap.getX();
            worldY = nextMap.getY();
            worldZ = nextMap.getZ();
        }


    }

    public static boolean gameLoaded;

    private void setNextCamera() {
        if(cameraMove != null)
            mapCameraMoves.offer(cameraMove);
        CameraMove nextMove = mapCameraMoves.poll();
        if(nextMove != null) {
            nextMove.reset();
            cameraMove = nextMove;
        }
    }

    private void startResourceProvider() {
        Thread t = new Thread(() ->  {
            while(!Client.gameLoaded) {
                client.processLoginOnDemandQueue();
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public void proceedToNextScene() {
        fadeToBlack.set(100);
    }


    public void resetMapData() {
        regions.clear();
    }

    public void resetSceneGraph() {
        loadedScenegraph = false;
        this.fadeFromBlack.set(100);
    }


    public void provideMap(Resource resource) {
        System.out.println("PROVIDED MAP");
        regions.stream()
                .filter(region -> region.getLandscape() == resource.ID)
                .forEach(region -> region.setLandscapeData(resource.buffer));
        regions.stream()
                .filter(region -> region.getObjects() == resource.ID)
                .forEach(region -> region.setObjectsData(resource.buffer, client.resourceProvider));
    }

    private boolean allMapsProvided() {
        return regions.stream().allMatch(region -> region.getObjectsData() != null && region.getLandscapeData() != null);
    }
    private List<MapRegionData> regions = Lists.newArrayList();




    private int worldX = 0, worldY = 0, worldZ = 0;

    @Getter
    @Setter
    private CameraMove cameraMove;
    @Getter
    private DoubleProperty fadeFromBlack = new SimpleDoubleProperty(100.0);
    @Getter
    private DoubleProperty fadeToBlack = new SimpleDoubleProperty(-1);

    private ConcurrentLinkedQueue<Vector3> mapPositions = Queues.newConcurrentLinkedQueue();
    private ConcurrentLinkedQueue<CameraMove> mapCameraMoves = Queues.newConcurrentLinkedQueue();

    private Sprite blackWindow;

    public Camera getCamera() {
        return cameraMove.getCamera();
    }

    public void resetFade() {
        this.fadeFromBlack.set(100);
    }

    public void resizeFade() {
        blackWindow = new Sprite(Client.canvasWidth, Client.canvasHeight);
        Arrays.fill(blackWindow.myPixels, (255 << 24 ) + 0x050505);
    }

}