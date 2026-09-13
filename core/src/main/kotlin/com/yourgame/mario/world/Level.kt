package com.yourgame.mario.world

import com.badlogic.gdx.math.Rectangle

/** Hand-authored side-scrolling adventure. */
class Level(layout: List<String>, val tileSize: Float=32f) {
    val widthInTiles=layout.maxOf{it.length};val heightInTiles=layout.size
    val solidTiles=mutableListOf<Rectangle>();val coinSpawns=mutableListOf<Rectangle>();val enemySpawns=mutableListOf<Rectangle>()
    var playerStart=Rectangle(0f,0f,tileSize,tileSize);private set
    val widthInPixels get()=widthInTiles*tileSize;val heightInPixels get()=heightInTiles*tileSize
    init{for(row in layout.indices){val line=layout[row];val worldY=(heightInTiles-1-row)*tileSize;for(col in line.indices){val worldX=col*tileSize;when(line[col)){'#'->solidTiles.add(Rectangle(worldX,worldY,tileSize,tileSize));'C'->coinSpawns.add(Rectangle(worldX+tileSize/4f,worldY+tileSize/4f,tileSize/2f,tileSize/2f));'E'->enemySpawns.add(Rectangle(worldX,worldY,tileSize,tileSize));'P'->playerStart=Rectangle(worldX,worldY,tileSize,tileSize)}}}}
    companion object{
        fun level1():Level{
            val layout=listOf(
                "................................................................................................................",
                "................................................................................................................",
                "...............C....................C.......................C....................C..............................",
                "..........#######..............#######................#######.................#######..................C..........",
                "................................................................................................................",
                "P.......E........C..C..C.....E..............C.....C.....E..............C..C.....E.............C.....C.......E......",
                "##########..##############...###############..###############...#############..#################..##############"
            )
            return Level(layout)
        }
    }
}
