function out = GetCenterPos(newTime, p1, p2, p3, stdEdge, angle)
% Get the Center Position with given p1,p2,p3, and return [time x y z].

[row col] = size(p1);
edges = [GetDistance(p1, p2) GetDistance(p1, p3) GetDistance(p2, p3)];

edges = sortrows(edges')';
stdLen = stdEdge(4);

centerPos = zeros(row, 4);
num = 0;
for r = 1:row
    num = num + 1;
    
    % Naive method 1: equation set
    ratios = edges(r, :) ./ stdEdge(1, 1:3);
    ratio = mean(ratios);
    currLen = stdLen * ratio;
    x0 = p1(r, :) + [0 0 10];
    [X, FV, EF, OUTPUT] = fsolve(@(x)distEquation(x, [p1(r, :); p2(r, :); p3(r, :)],...,
        currLen), x0);
    
    centerPos(num, 1) = newTime(r);
    centerPos(num, 2:4) = X;
    % out(r, 2:4) = X;
end

out = centerPos(1:num, :);
end

function out = distEquation(x, pos, len)
out = zeros(3);
for i = 1:3
    out(i) = GetDistance(x, pos(i, :)) - len;
end
end