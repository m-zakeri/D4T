import networkx as nx
import matplotlib.pyplot as plt
g = nx.read_gml('dependency_graph.gml')
nx.draw(g,with_labels=True)
plt.show()