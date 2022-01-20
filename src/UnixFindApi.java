import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


abstract class FD{
    public String name;
    public boolean isDirectory;
    public int size;
}

class File extends FD{
    File(String n, String ext){
        name = n;
        extension = ext;
        super.isDirectory = false;
    }

    public byte[] content;
    public String extension;
}

class Directory extends FD{

    Directory(String n){
        super.name = n;
        super.isDirectory = true;
    }

    public List<FD> fileDirectories;
}


abstract class Filter{
    boolean isValid(File file){
        return false;
    }
}

class NameFilter extends Filter{
    String rName;

    NameFilter(String name){
        rName = name;
    }

    @Override
    public boolean isValid(File file){
        return file.name == rName;
    }
}

class ExtensionFilter extends Filter{
    String rExt;

    ExtensionFilter(String extension){
        rExt = extension;
    }

    @Override
    public boolean isValid(File file){
        return file.extension == rExt;
    }
}

enum SizeComparator{
    GREATER_THAN,
    LESS_THAN,
    GREATER_EQUAL_THAN,
    LESS_EQUAL_THAN

}

class SizeFilter extends Filter{
    int rSize;
    SizeComparator comparatorExpression;

    SizeFilter(int size, SizeComparator sizeComparator){
        size = rSize;
        comparatorExpression = sizeComparator;
    }

    @Override
    public boolean isValid(File file){

        switch (comparatorExpression){
            case GREATER_THAN:
                return file.size > rSize;
            case GREATER_EQUAL_THAN:
                return file.size >= rSize;
            case LESS_THAN:
                return file.size < rSize;
            case LESS_EQUAL_THAN:
                return file.size <= rSize;
        }
        return false;
    }
}

enum FilterLogicalOperator{
    AND,
    OR
}

class SearchCriteria{
    List<Filter> filters;
    FilterLogicalOperator filterLogicalOperator;

    SearchCriteria(List<Filter> lf, FilterLogicalOperator flo){
        filters = lf;
        filterLogicalOperator = flo;
    }

    public boolean validateFilters(File file){
        switch (filterLogicalOperator) {
            case AND:
                return validateAndOperator(file);
            case OR:
                return validateOrOperator(file);
        }

        return false;
    }

    public boolean validateAndOperator(File file){
        var answer = true;
        for(var i = 0; i < filters.size(); i++){
            if(!filters.get(i).isValid(file)){
                answer = false;
            }
        }

        return answer;
    }

    public boolean validateOrOperator(File file){
        for(var i = 0; i < filters.size(); i++){
            if(filters.get(i).isValid(file)){
                return true;
            }
        }
        return false;
    }
}


class FileSystem{

    FD fileDirectory;

    public FileSystem(FD fd){
        fileDirectory = fd;
    }

    public List<File> find(String directoryPath, SearchCriteria searchCriteria){
        var dpNode = changeDirectory(directoryPath);

        var files = new ArrayList<File>();

        find(dpNode, searchCriteria, files);


        return files;
    }

    public void find(FD dir, SearchCriteria searchCriteria, List<File> files){
        if(!dir.isDirectory){
            var currentFile = (File) dir;
            if(searchCriteria.validateFilters(currentFile)){
                files.add(currentFile);
            }


            return;
        }

        var currentDirectory = (Directory) dir;

        for(var i = 0; i < currentDirectory.fileDirectories.size(); i++){
            var cd = currentDirectory.fileDirectories.get(i);
            find(cd, searchCriteria, files);
        }
    }

    public FD changeDirectory(String directoryPath){

        if(directoryPath.startsWith("/")){
            //we already started at root path /
            directoryPath = directoryPath.substring(1, directoryPath.length());
        }

        var dir = changeDirectoryDFS(fileDirectory, directoryPath.split("/"), 0);
        return dir;
    }

    public FD changeDirectoryDFS(FD fd, String[] paths, int depth){
        FD answer = null;

        if(fd.isDirectory && fd.name.equals(paths[paths.length - 1]) && depth == paths.length){
            return fd;
        }


        if(fd.isDirectory) {
            var currentDirectory = (Directory) fd;
            for (var i = 0; i < currentDirectory.fileDirectories.size(); i++) {
                var cDir = currentDirectory.fileDirectories.get(i);
                if (cDir.name.equals(paths[depth]) && cDir.isDirectory) {
                    answer = changeDirectoryDFS(cDir, paths, depth + 1);
                }
            }
        }

        //if you reach here, that means what you trying to go to either dont exist or aint a directory
        return answer;
    }


}

public class UnixFindApi {
    public static void main(String[] args){
        //                                         ""
        //                        /                  \             \
        //                    examples              josh         test.png
        //              /        /      \               |
        //           learn     f1.xml   f2.xml         hello.java
        //           /      \
        //      book1.pdf   book2.pdf

        Directory d = new Directory("");
        d.fileDirectories = Arrays.asList(new Directory("examples"), new Directory("josh"), new File("test", "png"));

        Directory examples = (Directory) d.fileDirectories.get(0);
        examples.fileDirectories = Arrays.asList(new Directory("learn"), new File("f1", "xml"), new File("f2", "xml"));

        Directory josh = (Directory) d.fileDirectories.get(1);
        josh.fileDirectories = Arrays.asList(new File("hello", "java"));

        Directory learn = (Directory) examples.fileDirectories.get(0);
        learn.fileDirectories = Arrays.asList(new File("book1", "pdf"), new File("book2", "pdf"));

        FileSystem fs = new FileSystem(d);

        SearchCriteria sc = new SearchCriteria(Arrays.asList(new ExtensionFilter("pdf")), FilterLogicalOperator.AND);

        var myFiles = fs.find("/examples/learn", sc);



    }

}
