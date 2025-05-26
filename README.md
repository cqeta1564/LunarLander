# Nástroj pro Vektorové Body v Obrázku

Jednoduchá Java Swing aplikace, která umožňuje uživateli načíst rastrový obrázek, klikáním na něj přidávat vektorové body, přibližovat a oddalovat zobrazení, odvolat poslední přidaný bod a exportovat souřadnice všech bodů do textového souboru.

## Funkce

* **Načtení obrázku**: Podpora pro běžné rastrové formáty (JPG, PNG, GIF).
* **Přidávání bodů**: Jednoduchým kliknutím myši na obrázek lze přidat vektorový bod. Body jsou vizualizovány jako červené kroužky.
* **Přiblížení/Oddálení (Zoom)**: Možnost plynule měnit velikost zobrazení obrázku pomocí tlačítek.
* **Odvolání akce (Undo)**: Možnost odstranit poslední přidaný bod.
* **Export bodů**: Souřadnice všech přidaných bodů (relativní k původní velikosti obrázku) lze exportovat do textového souboru (`.txt`). Každý bod je na novém řádku ve formátu `X,Y`.
* **Posuvníky (Scrollbars)**: Pokud je přiblížený obrázek větší než okno, automaticky se zobrazí posuvníky.

## Předpoklady

* Nainstalovaná Java Development Kit (JDK), verze 8 nebo vyšší.

## Sestavení a Spuštění

1.  **Sestavení (Kompilace)**:
    Otevřete terminál nebo příkazový řádek, přejděte do adresáře, kde je uložen soubor `VectorPointAppZoomUndo.java`, a spusťte příkaz:
    ```bash
    javac VectorPointAppZoomUndo.java
    ```
    Tím se vytvoří soubor `VectorPointAppZoomUndo.class`.

2.  **Spuštění aplikace**:
    Ve stejném adresáři spusťte příkaz:
    ```bash
    java VectorPointAppZoomUndo
    ```

## Jak používat aplikaci

1.  **Načtení obrázku**:
    * Klikněte na tlačítko "Načíst obrázek" nebo použijte menu "Soubor" -> "Načíst obrázek...".
    * V dialogovém okně vyberte požadovaný obrázkový soubor. Obrázek se zobrazí v hlavním panelu.

2.  **Přidávání bodů**:
    * Po načtení obrázku klikněte levým tlačítkem myši na libovolné místo v obrázku, kam chcete přidat bod.
    * Na místě kliknutí se zobrazí červený kroužek.

3.  **Přiblížení a Oddálení**:
    * Použijte tlačítka "Přiblížit (+)" a "Oddálit (-)" pro změnu úrovně přiblížení obrázku.
    * Pokud je obrázek větší než viditelná oblast, objeví se posuvníky.

4.  **Odvolání posledního bodu**:
    * Klikněte na tlačítko "Odvolat poslední bod". Tím se odstraní naposledy přidaný bod. Tlačítko je aktivní pouze pokud existuje alespoň jeden bod.

5.  **Export bodů**:
    * Klikněte na tlačítko "Exportovat body" nebo použijte menu "Soubor" -> "Exportovat body...".
    * V dialogovém okně vyberte název a umístění pro textový soubor (standardně `vektorove_body.txt`).
    * Souřadnice bodů budou uloženy ve formátu `X,Y` (každý bod na novém řádku). Souřadnice odpovídají pozici bodu na původním, neškálovaném obrázku.

## Možná budoucí vylepšení

* Možnost mazání konkrétních bodů (nejen posledního).
* Editace pozice existujících bodů.
* Zoom pomocí kolečka myši.
* Ukládání a načítání celého projektu (obrázek + body).
* Více možností vizualizace bodů (barva, velikost, tvar).

---
*Tato aplikace byla vytvořena jako ukázkový projekt.*