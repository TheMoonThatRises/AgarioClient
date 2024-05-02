package ceccs.game.utils;

import ceccs.game.objects.ui.Blob;
import ceccs.network.utils.CustomID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ConsolidateBlobs {

    @SafeVarargs
    public static ArrayList<Blob> convert(Map<CustomID, ? extends Blob>... blobs) {
        ArrayList<Blob> output = new ArrayList<>();

        Arrays.stream(blobs)
                .parallel()
                .forEach(blobBlob -> output.addAll(
                        blobBlob.values()
                                .stream()
                                .toList()
                ));

        return output;
    }

}
