import kotlin.math.max
import kotlin.math.min

data class MinesweeperBoard(val board: List<String>) {

    // Proper constructor: pass a list of Strings -> listOf("·*·*·,··*··, etc")

    // TODO("Implement this function to complete the task")
    fun withNumbers(): List<String> {

        //If board is a empty list
        if(board.isEmpty()) return board

        val numRows = board.size
        val numCols = board[0].length
        val chunkedBoard = board.map{ it.chunked(1)}

        fun countMines(row: Int, col: Int): Char {

            var count = 0

            for (c in max(0, col-1)..min(col+1, numCols-1)){
                for(r in max(0, row-1)..min(row+1, numRows-1)){

                    if(chunkedBoard[r][c] == "*"){
                        count++
                    }
                }
            }

            return if(count > 0) (count+48).toChar() else ' '
        }

        /* al countedBoard = chunkedBoard.mapIndexed{ row, line ->
            line.mapIndexed{ col, cell -> {
                    when(cell){
                        " " -> countMines(row,col)
                        else -> cell
                    }
                }
            }.joinToString(separator = "")
        } */

        val numberedBoard = board.mapIndexed{ row,str ->
            str.mapIndexed{ col, cell ->
                when(cell){
                    ' ' ->  countMines(row, col)
                    else -> cell// We assume cells are only ' ' or '*'
                }
            }.joinToString(separator = "")
        }

        return numberedBoard
    }

}