package com.college.server;
import java.util.*;

public class AdvancedClassifier {

    private Map<String, Map<String, Integer>> wordCounts = new HashMap<>();
    private Map<String, Integer> classCounts = new HashMap<>();
    private Set<String> vocabulary = new HashSet<>();
    private int totalDocs = 0;
	
    public void trainModel() {

        /*Map<String, String> trainingData = Map.of(
            "Complete calculus lecture tutorial", "Educational",
            "Data structures algorithms course", "Educational",
            "Krishna bhajan devotional song", "Devotional",
            "Temple morning aarti prayer", "Devotional",
            "Latest Bollywood music official song", "Music",
            "Top album new hit song", "Music"
        );*/
		
		Map<String, String> trainingData = Map.ofEntries(

		Map.entry("Complete calculus lecture tutorial", "Educational"),
		Map.entry("Data structures algorithms course", "Educational"),
		Map.entry("Operating system full lecture series", "Educational"),
		Map.entry("Machine learning introduction tutorial", "Educational"),
		Map.entry("Artificial intelligence basics explained", "Educational"),
		Map.entry("Java programming full course", "Educational"),
		Map.entry("Python programming beginner tutorial", "Educational"),
		Map.entry("Computer networks full lecture", "Educational"),
		Map.entry("Discrete mathematics concepts lecture", "Educational"),
		Map.entry("Linear algebra lecture tutorial", "Educational"),
		Map.entry("Database management system course", "Educational"),
		Map.entry("Statistics probability lecture class", "Educational"),
		Map.entry("Physics quantum mechanics lecture", "Educational"),
		Map.entry("Chemistry organic reactions tutorial", "Educational"),
		Map.entry("History world war documentary lecture", "Educational"),
		Map.entry("Economics supply demand theory class", "Educational"),
		Map.entry("SEX","other"),
		
		Map.entry("machine learning tutorial", "Educational"),
		Map.entry("deep learning neural networks lecture", "Educational"),
		Map.entry("artificial intelligence course", "Educational"),
		Map.entry("neural network training explanation", "Educational"),
		Map.entry("supervised learning algorithm tutorial", "Educational"),
		Map.entry("python machine learning tutorial", "Educational"),
		Map.entry("data science full course", "Educational"),
		Map.entry("deep learning specialization lecture", "Educational"),
		Map.entry("computer vision convolutional neural network", "Educational"),
		Map.entry("natural language processing tutorial", "Educational"),
		
		// Add these to your Map.entry list
		Map.entry("Pooling Layers Convolutional Neural Networks", "Educational"),
		Map.entry("C4W1L09 Deep Learning Course", "Educational"),
		Map.entry("Max pooling average pooling explained", "Educational"),
		Map.entry("CNN architecture lecture", "Educational"),
		Map.entry("Convolutional Neural Networks CNN lecture", "Educational"),
		Map.entry("Backpropagation and gradient descent explained", "Educational"),
		Map.entry("Pooling layers max pooling average pooling", "Educational"),
		Map.entry("Activation functions ReLU Sigmoid Softmax", "Educational"),
		Map.entry("Object detection and image segmentation tutorial", "Educational"),
		Map.entry("Recurrent Neural Networks RNN LSTM", "Educational"),
		Map.entry("Transformer models and Attention mechanism", "Educational"),
		Map.entry("Generative Adversarial Networks GANs", "Educational"),
		Map.entry("University lecture series semester", "Educational"),
		Map.entry("C1W1 L01 introduction to algorithms", "Educational"), // Pattern for Coursera/DeepLearning.AI
		Map.entry("MIT OpenCourseWare official lecture", "Educational"),
		Map.entry("Stanford CS231n Convolutional Neural Networks", "Educational"),
		Map.entry("Coding bootcamp full stack development", "Educational"),
		Map.entry("C1W1L01 Neural Networks and Deep Learning", "Educational"),
		Map.entry("C2W3L05 Hyperparameter tuning and Batch Norm", "Educational"),
		Map.entry("C4W2L03 ResNet Case Study", "Educational"),
		Map.entry("C5W1L08 Gated Recurrent Units GRU", "Educational"),
		Map.entry("Vanishing Gradient Problem and Exploding Gradients", "Educational"),
		Map.entry("Softmax Regression and Cross Entropy Loss", "Educational"),
		Map.entry("Data Augmentation for Image Recognition", "Educational"),
		Map.entry("Word Embeddings and Word2Vec Tutorial", "Educational"),
		// DeepLearning.AI / Coursera Specific Patterns
		Map.entry("C4W1L09 Pooling Layers", "Educational"),
		Map.entry("C4W1L07 Padding and Strided Convolutions", "Educational"),
		Map.entry("C4W1L08 Convolutions Over Volume", "Educational"),
		Map.entry("C4W1L10 Convolutional Neural Networks Example", "Educational"),
		Map.entry("C1W2L01 Neural Networks Basics", "Educational"),
		Map.entry("C1W3L04 Activation Functions", "Educational"),
		Map.entry("C2W1L01 Train Dev Test sets", "Educational"),
		Map.entry("C2W3L05 Batch Normalization", "Educational"),
		Map.entry("C5W1L01 Why Sequence Models", "Educational"),

		// Technical Architecture Components
		Map.entry("Max Pooling and Average Pooling Explained", "Educational"),
		Map.entry("Vanishing Gradient Problem and Solutions", "Educational"),
		Map.entry("ReLU Activation and Leaky ReLU", "Educational"),
		Map.entry("Softmax Layer and Multi-class Classification", "Educational"),
		Map.entry("Dropout Regularization to Prevent Overfitting", "Educational"),
		Map.entry("Fully Connected Layer and Flattening", "Educational"),
		Map.entry("Weights and Biases Initialization", "Educational"),

		// Mathematical & Statistical Foundations
		Map.entry("Linear Regression and Gradient Descent", "Educational"),
		Map.entry("Backpropagation Calculus Derivation", "Educational"),
		Map.entry("Cost Function and Loss Function Analysis", "Educational"),
		Map.entry("Vectorized Implementation of Logistic Regression", "Educational"),
		
		
		Map.entry("Nudes hot pick 18+","other"),
		Map.entry("Sri Sathya Sai Institute of higher Learning","Educational"),
		Map.entry("Krishna bhajan devotional song", "Devotional"),
		Map.entry("Temple morning aarti prayer", "Devotional"),
		Map.entry("Shiva bhakti devotional music", "Devotional"),
		Map.entry("Hanuman chalisa devotional prayer", "Devotional"),
		Map.entry("Lord Vishnu bhajan spiritual song", "Devotional"),
		Map.entry("Durga maa devotional chant", "Devotional"),
		Map.entry("Radha Krishna spiritual bhajan", "Devotional"),
		Map.entry("Sai baba devotional song", "Devotional"),
		Map.entry("Temple evening prayer chant", "Devotional"),
		Map.entry("Spiritual meditation mantra music", "Devotional"),
		Map.entry("Hindu devotional aarti bhajan", "Devotional"),
		Map.entry("Divine chanting meditation prayer", "Devotional"),
		Map.entry("Prashanti Mandir Live","Devotional"),
		Map.entry("Sathya Sai Baba","Devotional"),
		Map.entry("Latest Bollywood music official song", "Music"),
		Map.entry("Top album new hit song", "Music"),
		Map.entry("Romantic Bollywood love song", "Music"),
		Map.entry("Hindi sad song official video", "Music"),
		Map.entry("Punjabi dance music video", "Music"),	
		Map.entry("English pop music official video", "Music"),
		Map.entry("Rock band live concert song", "Music"),
		Map.entry("DJ remix dance music", "Music"),
		Map.entry("Classical instrumental music performance", "Music"),
		Map.entry("Hip hop rap music video", "Music"),
		Map.entry("Acoustic guitar cover song", "Music"),
		Map.entry("Indian classical music performance", "Music"),
		Map.entry("Funny comedy video compilation", "Entertainment"),
		Map.entry("Movie trailer official teaser", "Entertainment"),
		Map.entry("Stand up comedy performance", "Entertainment"),
		Map.entry("Gaming live stream highlight", "Entertainment"),
		Map.entry("Football match highlights", "Entertainment")
		);
        for (String text : trainingData.keySet()) {

            String category = trainingData.get(text);
            classCounts.put(category,
                    classCounts.getOrDefault(category, 0) + 1);

            wordCounts.putIfAbsent(category, new HashMap<>());
            totalDocs++;

            for (String word : tokenize(text)) {
                vocabulary.add(word);
                Map<String, Integer> counts = wordCounts.get(category);
                counts.put(word, counts.getOrDefault(word, 0) + 1);
            }
        }
    }

    public String predict(String text) {

        double maxScore = Double.NEGATIVE_INFINITY;
        String bestClass = null;

        for (String category : classCounts.keySet()) {

            double logProb = Math.log((double) classCounts.get(category) / totalDocs);

            Map<String, Integer> counts = wordCounts.get(category);
            int totalWords = counts.values().stream().mapToInt(i -> i).sum();

            for (String word : tokenize(text)) {

                int freq = counts.getOrDefault(word, 0);

                double prob = (freq + 1.0) /
                        (totalWords + vocabulary.size());

                logProb += Math.log(prob);
            }

            if (logProb > maxScore) {
                maxScore = logProb;
                bestClass = category;
            }
        }

        return bestClass != null ? bestClass : "Other";
    }

    private List<String> tokenize(String text) {
        return Arrays.asList(
                text.toLowerCase()
                    .replaceAll("[^a-z ]", "")
                    .split("\\s+"));
    }

    public boolean isAllowed(String category) {
        return category.equals("Educational")
                || category.equals("Devotional")
                || category.equals("Music");
    }
}