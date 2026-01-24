package Main;
// ============================= IMPORTAÇÕES =============================
import Entity.Player;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
//============================= INICIO =============================
public class GameMap {

    private Player player;
    //=============================DECLARAÇÃO DAS IMAGENS=============================
    private BufferedImage CeuImg;
    private BufferedImage NuvemImg1;
    //private BufferedImage NuvemImg2;
    private BufferedImage GramaImg;
    private BufferedImage ArvoreImg;

    // =============================DECLARAÇÃO DAS ESCALAS DAS IMAGENS=============================
    private BufferedImage ArvoreEscalada;
    private BufferedImage EscalaCeu;
    private BufferedImage EscalaNuvemLonge1, EscalaNuvemMedia1, EscalaNuvemPerto1;
    //private BufferedImage EscalaNuvemLonge2, EscalaNuvemMedia2, EscalaNuvemPerto2;
    private BufferedImage EscalaGrama;

    // =============================DECLARAÇÃO DAS CAMADAS PARALLAX=============================
    private ParallaxLayer NuvensLonge1, NuvensMedia1, NuvensPerto1;
    //private ParallaxLayer NuvensLonge2, NuvensMedia2, NuvensPerto2;
    private ParallaxLayer camadaGrama;

    //=============================ALTURA DO CHÃO=============================
    private final int plataformaAltura = 150;

    //=============================CAMERA SINCRONIZADA AO PLAYER=============================
    private double cameraX = 0;

    // =============================RESOLUÇÃO=============================
    private int lastW = -1;
    private int lastH = -1;

    //=============================GERAÇÃO DE ÁRVORES ALEATÓRIAS=============================
    /*private class Arvore {
        int x;
        int y;
        public Arvore(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    private java.util.List<Arvore> arvores = new java.util.ArrayList<>();
    private java.util.Random random = new java.util.Random();
    */
//=============================COMEÇO DO CONSTRUTOR=============================
    public GameMap() {
        //=============================CARREGAMENTO DAS IMAGENS=============================
        try { CeuImg = ImageIO.read(getClass().getResourceAsStream("/res/CEU.jpg")); } catch (Exception ignored) {}
        try { NuvemImg1 = ImageIO.read(getClass().getResourceAsStream("/res/NUVEM.png")); } catch (Exception ignored) {}
        //try { NuvemImg2 = ImageIO.read(getClass().getResourceAsStream("/res/NUVEMDOIS.png")); } catch (Exception ignored) {}
        try { GramaImg = ImageIO.read(getClass().getResourceAsStream("/res/GRAMAPIXEL.png")); } catch (Exception ignored) {}
        try { ArvoreImg = ImageIO.read(getClass().getResourceAsStream("/res/ARVORE.png")); } catch (Exception ignored) {}

    }
    // =============================GERAR ÁRVORES ALEATÓRIAS=============================

    private void gerarArvores(int quantidade, int mapStart, int mapEnd, int groundY) {

        int spacing = 300; // =============================DISTANCIA MÍNIMA ENTRE AS ÁRVORES=============================
        quantidade = 10;
        for (int i = 0; i < quantidade; i++) {

            int x;
            boolean valido;

            // =============================GERA AS ARVORES DE ACORDO COM O ESPAÇAMENTO=============================
            /*do {
                valido = true;
                x = random.nextInt(mapEnd - mapStart) + mapStart;

                for (Arvore a : arvores) {
                    if (Math.abs(a.x - x) < spacing) {
                        valido = false;
                        break;
                    }
                }

            } while (!valido);

             */

            // =============================ALTURA DA ARVORE=============================
            //int treeHeight = (ArvoreEscalada != null ? ArvoreEscalada.getHeight() : 150);

            // =============================DEIXA A ARVORE PERTO DO CHAO=============================
            //int y = groundY - treeHeight + 50;

            // =============================ADICIONA A ARVORE PROPRIAMENTE DITA=============================
            //arvores.add(new Arvore(x, y));
        }
    }
//=============================SETA O PLAYER=============================
    public void setPlayer(Player p) {
        this.player = p;
    }

    // ============================================================
    // DRAW PRINCIPAL
    // ============================================================
    public void draw(Graphics2D g, int width, int height, Player player) {

        this.player = player;

        // =============================RESOLUÇÃO=============================
        if (width != lastW || height != lastH) {
            rescaleImages(width, height);
            lastW = width;
            lastH = height;
        }

        // =============================FUNDO DO CÉU=============================
        if (EscalaCeu != null)
            g.drawImage(EscalaCeu, 0, 0, null);
        else {
            g.setColor(new Color(100, 150, 255));
            g.fillRect(0, 0, width, height);
        }

        // =============================CONJUNTO DE NUVEM PARA PARALLAX=============================
        // Nuvens conjunto 1
        if (NuvensLonge1 != null) NuvensLonge1.drawTiled(g, width, height, (int)cameraX);
        if (NuvensMedia1 != null) NuvensMedia1.drawTiled(g, width, height, (int)cameraX);
        if (NuvensPerto1 != null) NuvensPerto1.drawTiled(g, width, height, (int)cameraX);
        // ============================= DESENHAR ÁRVORES =============================
       /*
        if (ArvoreEscalada != null) {
            for (Arvore a : arvores) {
                int telaX = a.x - cameraX; // converter worldX → screenX
                if (telaX > -200 && telaX < width + 200) {
                    g.drawImage(ArvoreEscalada, telaX, a.y, null);
                }
            }
        }
        */

        // ============================= DESENHAR GRAMA / CHÃO =============================
        int groundY = height - plataformaAltura;
        if (camadaGrama != null) camadaGrama.drawLine(g, width, groundY, (int)cameraX);

    }
    // ============================================================
    // REESCALAR IMAGENS
    // ============================================================
    private void rescaleImages(int w, int h) {

        // =============================CEU OCUPA TUDO=============================
        if (CeuImg != null)
            EscalaCeu = resize(CeuImg, w, h);

        // =============================AMBIENTES SÃO REESCALADAS=============================
        // Nuvens conjunto 1
        if (NuvemImg1 != null) {

            int cloudH = h / 1;

            EscalaNuvemLonge1 = resizeByHeight(NuvemImg1, cloudH);
            EscalaNuvemMedia1 = resizeByHeight(NuvemImg1, cloudH);
            EscalaNuvemPerto1 = resizeByHeight(NuvemImg1, cloudH);

            NuvensLonge1 = new ParallaxLayer(EscalaNuvemLonge1, 0.15, 20, 0);
            NuvensMedia1 = new ParallaxLayer(EscalaNuvemMedia1, 0.35, 50, 0);
            NuvensPerto1 = new ParallaxLayer(EscalaNuvemPerto1, 0.55, 80, 0);
        }
        // Grama / chão
        if (GramaImg != null) {
            EscalaGrama = resizeByHeight(GramaImg, plataformaAltura);
            camadaGrama = new ParallaxLayer(EscalaGrama, 1.0, 0, 0);
        }
        // Arvores
        if (ArvoreImg != null) {
            int arvAlt = (int)(plataformaAltura * 2.2);
            ArvoreEscalada = resizeByHeight(ArvoreImg, arvAlt);
        }
        //Gera apenas 1 arvore
        /*if (arvores.isEmpty()) {
            int groundY = h - plataformaAltura;
            gerarArvores(40, 0, 5000, groundY);
        }*/
    }

    // ============================================================
    // RESIZE PARA REESCALAMENTO DAS IMAGENS
    // ============================================================
    private BufferedImage resize(BufferedImage img, int newW, int newH) {
        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, newW, newH, null);
        g.dispose();
        return out;
    }

    private BufferedImage resizeByHeight(BufferedImage img, int newH) {
        int newW = img.getWidth() * newH / img.getHeight();
        return resize(img, newW, newH);
    }

    // ============================================================
    // PARALLAX LAYER QUE CONTROLA CADA CAMADA DO JOGO DANDO EFEITO DE PROFUNDIDADE
    // ============================================================
    private class ParallaxLayer {

        private final BufferedImage img;
        private final double factor;
        private final int offsetY;
        private double offsetX;

        public ParallaxLayer(BufferedImage img, double factor, int offsetY, double offsetX) {
            this.img = img;
            this.factor = factor;
            this.offsetY = offsetY;
            this.offsetX = offsetX;
        }

        public void addOffsetX(double value) {
            this.offsetX += value;
        }
        // =============================PARA DESENHAR OS CEUS E ARVORES=============================
        public void drawTiled(Graphics g, int width, int height, int cameraX) {
            if (img == null) return;

            int iw = img.getWidth();
            double paralX = -(cameraX * factor) + offsetX;

            int startX = (int) (paralX % iw);
            if (startX > 0) startX -= iw;

            for (int x = startX; x < width; x += iw)
                g.drawImage(img, x, offsetY, null);
        }
        // =============================PARA DESENHAR O CHAO A GRAMA=============================
        public void drawLine(Graphics g, int width, int y, int cameraX) {
            if (img == null) return;

            int iw = img.getWidth();
            double paralX = -(cameraX * factor) + offsetX;

            int startX = (int) (paralX % iw);
            if (startX > 0) startX -= iw;

            for (int x = startX; x < width; x += iw)
                g.drawImage(img, x, y, null);
        }
    }
    public int getCameraX() {
        return (int) this.cameraX;
    }

    public void update(int playerWorldX, int screenWidth) {
        // Zona morta: Onde a câmera começa a andar
        int deadzone = screenWidth / 4; // Ou / 2, conforme seu gosto
        double targetX = 0;

        if (playerWorldX > deadzone) {
            targetX = playerWorldX - deadzone;
        } else {
            targetX = 0;
        }

        // 2. Aplica a suavização (Lerp)
        // O valor 0.05 significa que a câmera percorre 5% da distância a cada frame.
        // Ajuste entre 0.05 (muito suave/lento) e 0.2 (rápido).
        this.cameraX += (targetX - this.cameraX) * 0.05;
    }
}
