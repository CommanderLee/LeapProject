function [newT newP1 newP2 newP3] = AdjustPoints(timeArray, points)
% Ajust the p1, p2, p3 into correct order.
% In Leap's Coordinate: p1(0, j, 0), p2(i, 0, 0), p3(-i, 0, 0)
p1 = points(:, 2:4);
p2 = points(:, 6:8);
p3 = points(:, 10:12);
pointID = [points(:, 1) points(:, 5) points(:, 9)];
[row col] = size(p1);

newTime = zeros(row, 1);
newP = cat(3, zeros(row, 3), zeros(row, 3), zeros(row, 3));

num = 0;
order = [0 0 0];
last = 0;

for r = 1:row
    if sum(pointID(r, :) - [0 1 2]) == 0
        if last == 0
            last = 1;
            % Adjust the first points with the X
            [xmin imin] = min([p1(r, 1) p2(r, 1) p3(r, 1)]);
            [xmax imax] = max([p1(r, 1) p2(r, 1) p3(r, 1)]);
            imid = 1 + 2 + 3 - imax - imin;
            order = [imid imax imin];
        else
            last = r;
        end
        %naive solution: just sort according to the first order
        num = num + 1;
        newTime(num, 1) = timeArray(r, 1);
        for i = 1:3
            newP(num, 1:3, i) = points(r, (order(i) * 4 - 2):(order(i) * 4));
        end
%         newP1(num, 1:3) = points(r, (order(1) * 4 - 2):(imid * 4));
%         newP2(num, 1:3) = points(r, (imax * 4 - 2):(imax * 4));
%         newP3(num, 1:3) = points(r, (imin * 4 - 2):(imin * 4));
    end
end

newT = newTime(1:num, 1);
newP1 = newP(1:num, :, 1);
newP2 = newP(1:num, :, 2);
newP3 = newP(1:num, :, 3);
end