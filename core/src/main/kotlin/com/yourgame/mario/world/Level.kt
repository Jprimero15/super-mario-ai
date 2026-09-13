package com.yourgame.mario.world

import com.badlogic.gdx.math.Rectangle

/** Hand-authored long-form side-scrolling adventure. */
class Level(layout: List<String>, val tileSize: Float=32f) {
    val widthInTiles=layout.maxOf{it.length};val heightInTiles=layout.size
    val solidTiles=mutableListOf<Rectangle>();val coinSpawns=mutableListOf<Rectangle>();val enemySpawns=mutableListOf<Rectangle>()
    var playerStart=Rectangle(0f,0f,tileSize,tileSize);private set
    val widthInPixels get()=widthInTiles*tileSize
    val heightInPixels get()=heightInTiles*tileSize

    init{for(row in layout.indices){val line=layout[row];val worldY=(heightInTiles-1-row)*tileSize;for(col in line.indices){val worldX=col*tileSize;when(line[col]){'#'->solidTiles.add(Rectangle(worldX,worldY,tileSize,tileSize));'C'->coinSpawns.add(Rectangle(worldX+tileSize/4f,worldY+tileSize/4f,tileSize/2f,tileSize/2f));'E'->enemySpawns.add(Rectangle(worldX,worldY,tileSize,tileSize));'P'->playerStart=Rectangle(worldX,worldY,tileSize,tileSize)}}}}

    companion object{
        fun level1():Level{
            val width=180
            val rows=Array(7){CharArray(width){'.'}}
            fun put(row:Int,col:Int,ch:Char){if(row in rows.indices&&col in 0 until width)rows[row][col]=ch}
            fun platform(row:Int,start:Int,end:Int){for(x in start..end)put(row,x,'#')}

            // Continuous ground with deliberate gaps to make the long run active.
            for(x in 0 until width)put(6,x,'#')
            listOf(10..12,27..29,45..47,64..66,82..84,101..103,120..122,140..142,160..162).forEach{range->range.forEach{put(6,it,'.')}}

            // Elevated routes and alternating platform sections.
            platform(3,12,19);platform(3,34,42);platform(3,55,63);platform(3,76,84);platform(3,97,105);platform(3,119,127);platform(3,143,151);platform(3,166,175)
            platform(4,21,25);platform(4,49,53);platform(4,88,93);platform(4,130,135);platform(4,156,160)

            put(5,1,'P')
            listOf(8,18,31,43,57,70,86,99,112,126,138,153,168,177).forEach{put(5,it,'E')}
            listOf(6,9,13,16,22,24,29,36,39,46,51,58,62,68,74,80,89,92,100,104,110,117,121,128,134,141,146,154,158,165,170,176).forEach{put(5,it,'C')}
            listOf(15,37,60,79,102,124,148,172).forEach{put(2,it,'C')}

            return Level(rows.map{String(it)})
        }
    }
}
