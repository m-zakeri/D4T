package org.jetbrains.research.intellijdeodorant.ide.refactoring.extractMethod;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.research.intellijdeodorant.core.ast.decomposition.cfg.ASTSlice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.intellijdeodorant.core.ast.decomposition.cfg.CFGNode;
import org.jetbrains.research.intellijdeodorant.ide.refactoring.Refactoring;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.jetbrains.research.intellijdeodorant.utils.PsiUtils.*;

/**
 * Representation of a refactoring, which suggests to extract code into separate method.
 */
public class ExtractMethodCandidateGroup implements Refactoring {
    private final @NotNull
    SmartPsiElementPointer<PsiElement> method;
    private @NotNull
    final ArrayList<ASTSlice> candidates;
    private final @NotNull
    String qualifiedMethodName;

    /**
     * Creates refactoring instance.
     *
     * @param slices slice group that consist of candidates to extract.
     */
    public ExtractMethodCandidateGroup(Set<ASTSlice> slices) {
        this.method = toPointer(slices.iterator().next().getSourceMethodDeclaration());
        this.candidates = new ArrayList<>(slices);
        this.qualifiedMethodName = getHumanReadableName(method.getElement());
    }

    /**
     * Returns a method from which code is proposed to be extracted into a separate method.
     */
    public @NotNull
    PsiMethod getMethod() {
        return Optional.ofNullable((PsiMethod) method.getElement()).orElseThrow(() ->
                new IllegalStateException("Cannot get method. Reference is invalid."));
    }

    /**
     * Returns a method that is proposed to be moved in this refactoring.
     */
    public @NotNull
    Optional<PsiMethod> getOptionalMethod() {
        return Optional.ofNullable((PsiMethod) method.getElement());
    }

    @NotNull
    public ArrayList<ASTSlice> getCandidates() {
        return candidates;
    }

    @NotNull
    @Override
    public String getDescription() {
        Optional<PsiMethod> method = getOptionalMethod();
        if (!method.isPresent()) return "";
        String methodName = getHumanReadableName(method.get());
        StringBuilder sb = new StringBuilder();
        Iterator<ASTSlice> candidatesIterator = candidates.iterator();
        for (int i = 0; i < candidates.size(); i++) {
            ASTSlice slice = candidatesIterator.next();
            sb.append(methodName.replaceAll(",",";").replace("::",",")).append(',');
            List<CFGNode> nodes = slice.getBoundaryBlock().getAllNodesIncludingTry();
            Set<Long> set = new HashSet<>();
            for (CFGNode node :nodes)
            {

                PsiElement psiElement = node.getStatement().getStatement();
                String path = psiElement.getContainingFile().getVirtualFile().getPath();
                TextRange textRange = psiElement.getTextRange();
                long startOffset = getLine(path,textRange.getStartOffset());
                long endOffset = getLine(path,textRange.getEndOffset());
                for (long h = startOffset; h <= endOffset ; h++)
                    set.add(h);
//                sb.append(textRange.getStartOffset()).append(',');
//                sb.append(startOffset).append('\n');
//                sb.append(textRange.getEndOffset()).append(',');
//                sb.append(endOffset).append('\n');
            }
            List<Long> sortedList = new ArrayList<>(set);
            Collections.sort(sortedList);
//            for (Long aLong : sortedList) {
//                sb.append(aLong).append('#');
//            }
            sb.append(sortedList.toString().replaceAll(",","::").replace("[","").replace("]",""));
            if (i != candidates.size()-1)
                sb.append('\n');
        }
        return sb.toString();
    }

    private long getLine(String addr,long charOffset)
    {
        File file = new File(addr);
        long lineNum = 1;
        long charRead = 0;
        try (FileReader fr = new FileReader(file))
        {
            int content;
            while ((content = fr.read()) != -1) {
                if (charRead == charOffset)
                    return lineNum;
                else if ((char) content == '\n')
                    lineNum++;
                charRead++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @NotNull
    @Override
    public String getExportDefaultFilename() {
        return "Long-Method";
    }

    @Override
    public String toString() {
        return qualifiedMethodName;
    }
}
