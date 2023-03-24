data class MinesweeperBoard(val board: List<String>) {

    val length_x = 0;
    val length_y = 0;


    fun findMines(x : Int, y : Int): Int {
        var sum = 0;
        val initialx : Int = if(x-1 >= 0) x-1 else x;
        val initialy : Int = if(y-1 >= 0) y-1 else y;
        val endingy : Int = if(y+1 < board[0].length - 1) y+1 else board[0].length - 1;
        val endingx : Int = if (x+1 < board.size - 1) x+1 else board.size - 1;
        for (w:Int in initialy..endingy) { //y
            for(z:Int in initialx..endingx) { //x
                if(board[z][w].equals('*')) {
                    sum+=1;
                }
            }
        }
        return sum;
    }

    fun withNumbers(): List<String> {
        val numberedBoard:ArrayList<String> = arrayListOf()
        var computation = 0;
        for(x in 0..board.size - 1) {
           numberedBoard.add(x,"")
           for(y in 0..board[0].length - 1) {
               if(board[x][y].compareTo('*') == 0) {
                   numberedBoard[x] += "*"
                   continue
               }
               computation = findMines(x,y);
               numberedBoard[x] += if(computation == 0) " " else computation.toString();
           }
        }
        return  numberedBoard
    }
}
