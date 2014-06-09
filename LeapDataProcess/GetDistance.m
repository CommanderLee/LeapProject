function out = GetDistance(p1, p2)
% Get the Euclid Distance of point1 and point2

dist = p1 - p2;
out = sqrt(sum((dist .^ 2), 2));

end